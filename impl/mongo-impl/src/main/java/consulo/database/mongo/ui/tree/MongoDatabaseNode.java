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
import consulo.database.icon.DatabaseIconGroup;
import consulo.database.mongo.transport.MongoCollectionState;
import consulo.database.mongo.transport.MongoDatabaseState;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentationData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A MongoDB database. Its children are the collections and views, without the system collections ({@code system.*}).
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public class MongoDatabaseNode extends AbstractTreeNode<MongoDatabaseState> {
    private final DataSource myDataSource;

    public MongoDatabaseNode(Project project, DataSource dataSource, MongoDatabaseState state) {
        super(project, state);
        myDataSource = dataSource;
    }

    @RequiredReadAction
    @Override
    public Collection<? extends AbstractTreeNode> getChildren() {
        MongoDatabaseState state = getValue();

        List<AbstractTreeNode<?>> children = new ArrayList<>();
        for (MongoCollectionState collection : getVisibleCollections(state)) {
            children.add(new MongoCollectionNode(myProject, myDataSource, state.getName(), collection));
        }
        return children;
    }

    @Override
    protected void update(PresentationData presentation) {
        MongoDatabaseState state = getValue();

        presentation.setIcon(DatabaseIconGroup.nodesDatabase());
        presentation.addText(state.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        presentation.addText(" " + getVisibleCollections(state).size(), SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
    }

    private static List<MongoCollectionState> getVisibleCollections(MongoDatabaseState state) {
        List<MongoCollectionState> collections = new ArrayList<>();
        for (MongoCollectionState collection : state.getCollections()) {
            if (!collection.isSystem()) {
                collections.add(collection);
            }
        }
        return collections;
    }

    @Override
    public String toString() {
        return myDataSource.getId() + ":" + getValue().getName();
    }
}
