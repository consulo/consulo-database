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

import consulo.annotation.component.ExtensionImpl;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.datasource.ui.DataSourceTreeNodeProvider;
import consulo.database.mongo.MongoDbDataSourceProvider;
import consulo.database.mongo.transport.MongoDatabaseState;
import consulo.database.mongo.transport.MongoState;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;

import java.util.function.Consumer;

/**
 * Database tool window children of a MongoDB data source: its databases, read from the {@link MongoState} the last refresh cached.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@ExtensionImpl(id = "mongodb")
public class MongoDataSourceTreeNodeProvider implements DataSourceTreeNodeProvider {
    @Override
    public boolean accept(DataSource dataSource) {
        return dataSource.getProvider() instanceof MongoDbDataSourceProvider;
    }

    @Override
    public void fillTreeNodes(Project project, DataSource dataSource, Consumer<AbstractTreeNode<?>> consumer) {
        MongoState state = DataSourceTransportManager.getInstance(project).getDataState(dataSource);
        if (state == null) {
            return;
        }

        for (MongoDatabaseState databaseState : state.getDatabases().values()) {
            consumer.accept(new MongoDatabaseNode(project, dataSource, databaseState));
        }
    }
}
