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


package consulo.database.mongo.ui.tree;

import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.ui.DataSourceOpenableNode;
import consulo.database.icon.DatabaseIconGroup;
import consulo.database.mongo.transport.MongoCollectionState;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentationData;

import java.util.Collection;
import java.util.List;

/**
 * A MongoDB collection, view or time series collection. A double-click opens its documents.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public class MongoCollectionNode extends AbstractTreeNode<MongoCollectionState> implements DataSourceOpenableNode {
    private final DataSource myDataSource;
    private final String myDatabaseName;

    public MongoCollectionNode(Project project, DataSource dataSource, String databaseName, MongoCollectionState state) {
        super(project, state);
        myDataSource = dataSource;
        myDatabaseName = databaseName;
    }

    @Override
    public DataSource getDataSource() {
        return myDataSource;
    }

    @Override
    public String getDatabaseName() {
        return myDatabaseName;
    }

    /**
     * @return the collection name, which may contain {@code /} and {@code .}
     */
    @Override
    public String getChildId() {
        return getValue().getName();
    }

    @RequiredReadAction
    @Override
    public Collection<? extends AbstractTreeNode> getChildren() {
        return List.of();
    }

    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }

    @Override
    public boolean expandOnDoubleClick() {
        return false;
    }

    @Override
    protected void update(PresentationData presentation) {
        MongoCollectionState state = getValue();

        presentation.setIcon(state.isView() ? DatabaseIconGroup.nodesView() : DatabaseIconGroup.nodesTable());
        presentation.addText(state.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

        String type = state.getType();
        if (!MongoCollectionState.TYPE_COLLECTION.equals(type)) {
            presentation.addText(" " + type, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
        }
    }

    @Override
    public String toString() {
        return myDatabaseName + ":" + getValue().getName();
    }
}
