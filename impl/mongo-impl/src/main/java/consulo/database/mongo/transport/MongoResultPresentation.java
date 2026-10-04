/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package consulo.database.mongo.transport;

import consulo.annotation.component.ExtensionImpl;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.ui.DataSourceTransportResultPresentation;
import consulo.database.mongo.MongoDbDataSourceProvider;
import consulo.database.mongo.localize.MongoLocalize;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.Space;
import consulo.ui.TextAttribute;
import consulo.ui.TextItemPresentation;
import consulo.ui.Tree;
import consulo.ui.TreeExecutor;
import consulo.ui.TreeModel;
import consulo.ui.TreeNode;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.ScrollableLayout;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Interim result view of a collection until the data grid is published: the documents of the first page as a tree. A document
 * shows its top level fields in the column order of the result, a field shows {@code name: value} with its BSON type grayed, and an
 * object or array opens to its entries - array elements named by index. A missing field has no node, a {@code null} one shows
 * {@code null}.
 * <p>
 * To be deleted once the result is the data grid with its tree mode.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@ExtensionImpl(id = "mongodb")
public class MongoResultPresentation implements DataSourceTransportResultPresentation<MongoFetchResult> {
    /**
     * A node opening to more entries shows the first ones only - a document holds at most 16 MB, but one array may still be huge.
     */
    private static final int MAX_CHILDREN = 1000;

    private static final int MAX_PREVIEW_LENGTH = 200;
    private static final int MAX_VALUE_LENGTH = 1000;

    @Override
    public boolean accept(DataSource dataSource) {
        return dataSource.getProvider() instanceof MongoDbDataSourceProvider;
    }

    @RequiredUIAccess
    @Override
    public Component buildComponentForResult(MongoFetchResult result,
                                             Project project,
                                             DataSource dataSource,
                                             @Nullable String dbName,
                                             @Nullable String childId,
                                             Disposable parent) {
        Tree<Item> tree = Tree.create(Item.root(), new DocumentTreeModel(result), TreeExecutor.uiThread());
        Disposer.register(parent, tree.destroyHook());

        Label summary = Label.create(buildSummary(result));
        summary.paddingBuilder().allSet(Space.SMALL).apply();

        DockLayout layout = DockLayout.create();
        layout.top(summary);
        layout.center(ScrollableLayout.create(tree));
        return layout;
    }

    private static LocalizeValue buildSummary(MongoFetchResult result) {
        int shown = result.getDocuments().size();
        int fields = result.getFields().size();
        if (!result.hasMore()) {
            return MongoLocalize.resultSummaryAll(shown, fields);
        }

        long total = result.getTotalCount();
        if (total == MongoFetchResult.UNKNOWN_COUNT) {
            return MongoLocalize.resultSummaryUnknown(shown, fields);
        }
        return MongoLocalize.resultSummaryEstimated(shown, total, fields);
    }

    private enum ItemKind {
        ROOT,
        DOCUMENT,
        FIELD,
        MORE
    }

    /**
     * A node of the document tree. Nodes are compared by identity: the tree is built once and never refreshed.
     */
    private static final class Item {
        private final ItemKind myKind;
        private final String myName;
        private final @Nullable MongoValue myValue;
        private final int myMoreCount;

        private Item(ItemKind kind, String name, @Nullable MongoValue value, int moreCount) {
            myKind = kind;
            myName = name;
            myValue = value;
            myMoreCount = moreCount;
        }

        static Item root() {
            return new Item(ItemKind.ROOT, "", null, 0);
        }

        static Item document(int index, MongoValue document) {
            return new Item(ItemKind.DOCUMENT, String.valueOf(index + 1), document, 0);
        }

        static Item field(String name, MongoValue value) {
            return new Item(ItemKind.FIELD, name, value, 0);
        }

        static Item more(int count) {
            return new Item(ItemKind.MORE, "", null, count);
        }

        /**
         * An empty object or array is a leaf, as every scalar.
         */
        boolean isLeaf() {
            MongoValue value = myValue;
            if (myKind == ItemKind.ROOT) {
                return false;
            }
            if (value == null) {
                return true;
            }
            if (value.isDocument()) {
                return value.getFields().isEmpty();
            }
            if (value.isArray()) {
                return value.getItems().isEmpty();
            }
            return true;
        }
    }

    private static final class DocumentTreeModel implements TreeModel<Item> {
        private final MongoFetchResult myResult;
        private final List<String> myFieldNames;
        private final Set<String> myFieldNameSet;

        private DocumentTreeModel(MongoFetchResult result) {
            myResult = result;
            myFieldNames = result.getFieldNames();
            myFieldNameSet = new HashSet<>(myFieldNames);
        }

        @Override
        public void buildChildren(Function<Item, TreeNode<Item>> nodeFactory, @Nullable Item parentValue) {
            if (parentValue == null) {
                return;
            }

            for (Item child : getChildren(parentValue)) {
                TreeNode<Item> node = nodeFactory.apply(child);
                node.setLeaf(child.isLeaf());
                node.setRenderer(DocumentTreeModel::render);
            }
        }

        private List<Item> getChildren(Item parent) {
            List<Item> children = new ArrayList<>();

            MongoValue value = parent.myValue;
            if (parent.myKind == ItemKind.ROOT) {
                List<MongoValue> documents = myResult.getDocuments();
                for (int i = 0; i < documents.size(); i++) {
                    children.add(Item.document(i, documents.get(i)));
                }
            }
            else if (value == null) {
                return children;
            }
            else if (parent.myKind == ItemKind.DOCUMENT && value.isDocument()) {
                addTopLevelFields(children, value);
            }
            else if (value.isDocument()) {
                List<MongoField> fields = value.getFields();
                for (MongoField field : fields) {
                    if (!add(children, Item.field(field.name(), field.value()), fields.size())) {
                        break;
                    }
                }
            }
            else if (value.isArray()) {
                List<MongoValue> items = value.getItems();
                for (int i = 0; i < items.size(); i++) {
                    if (!add(children, Item.field(String.valueOf(i), items.get(i)), items.size())) {
                        break;
                    }
                }
            }
            return children;
        }

        /**
         * The fields of a document in the column order of the result - the order the data grid will show them in. A field the
         * document does not have gets no node.
         */
        private void addTopLevelFields(List<Item> children, MongoValue document) {
            Map<String, MongoValue> values = new LinkedHashMap<>();
            for (MongoField field : document.getFields()) {
                // the first of repeated names is the value of the field
                values.putIfAbsent(field.name(), field.value());
            }

            List<String> names = new ArrayList<>(values.size());
            for (String name : myFieldNames) {
                // a field the document does not have is missing, unlike a field holding null - it gets no node
                if (values.containsKey(name)) {
                    names.add(name);
                }
            }
            // a field the sample did not see, which cannot happen for the fetched page - kept for safety
            for (String name : values.keySet()) {
                if (!myFieldNameSet.contains(name)) {
                    names.add(name);
                }
            }

            for (String name : names) {
                MongoValue value = values.get(name);
                if (value != null && !add(children, Item.field(name, value), names.size())) {
                    break;
                }
            }
        }

        /**
         * @return {@code false} once the limit is reached - a last node tells how many entries are not shown
         */
        private static boolean add(List<Item> children, Item child, int total) {
            if (children.size() == MAX_CHILDREN) {
                children.add(Item.more(total - MAX_CHILDREN));
                return false;
            }

            children.add(child);
            return true;
        }

        private static void render(Item item, TextItemPresentation presentation) {
            MongoValue value = item.myValue;
            switch (item.myKind) {
                case ROOT -> {
                }
                case MORE -> presentation.append(MongoLocalize.resultMoreChildren(item.myMoreCount), TextAttribute.GRAYED);
                case DOCUMENT -> {
                    presentation.append(item.myName, TextAttribute.REGULAR_BOLD);
                    if (value != null) {
                        presentation.append("  ");
                        presentation.append(MongoValueText.format(value, MAX_PREVIEW_LENGTH), TextAttribute.REGULAR);
                    }
                }
                case FIELD -> {
                    presentation.append(item.myName, TextAttribute.REGULAR_BOLD);
                    if (value != null) {
                        presentation.append(": ");

                        boolean container = value.isDocument() || value.isArray();
                        String text = MongoValueText.format(value, container ? MAX_PREVIEW_LENGTH : MAX_VALUE_LENGTH);
                        presentation.append(text, MongoValueMapper.isNull(value) ? TextAttribute.REGULAR_ITALIC : TextAttribute.REGULAR);

                        presentation.append("  ");
                        presentation.append(MongoValueMapper.getTypeName(value), TextAttribute.GRAYED);
                    }
                }
            }
        }
    }
}
