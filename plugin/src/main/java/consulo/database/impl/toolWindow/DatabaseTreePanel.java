/*
 * Copyright 2013-2020 consulo.io
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

package consulo.database.impl.toolWindow;

import consulo.component.messagebus.MessageBusConnection;
import consulo.dataContext.DataSink;
import consulo.dataContext.UiDataProvider;
import consulo.database.datasource.editor.DataSourceEditorManager;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.jdbc.ui.tree.DatabaseJdbcTableNode;
import consulo.database.datasource.model.DataSourceListener;
import consulo.database.datasource.transport.DataSourceTransportListener;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.DataSourceWorkspaceManager;
import consulo.database.impl.toolWindow.node.DatabaseSourceNode;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.Tree;
import consulo.ui.TreeNode;
import consulo.ui.ex.tree.TreeStructureWrappenModel;
import consulo.ui.ex.tree.UITreeState;
import consulo.ui.layout.ScrollableLayout;
import consulo.ui.layout.ScrollableLayoutOptions;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseTreePanel implements Disposable, UiDataProvider {
    private static final Logger LOG = Logger.getInstance(DatabaseTreePanel.class);

    private consulo.ui.Component myRootPanel;
    private final consulo.ui.Tree<Object> myTree;

    public DatabaseTreePanel(@Nonnull Project project) {
        DataSourceWorkspaceManager workspaceManager = DataSourceWorkspaceManager.getInstance(project);

        DatabaseTreeStructure structure = new DatabaseTreeStructure(project);
        TreeStructureWrappenModel<Object> wrapper = new TreeStructureWrappenModel<>(structure);
        myTree = Tree.create(wrapper.getRootElement(), wrapper);
        Disposer.register(this, myTree.destroyHook());
        myTree.addDoubleClickListener(e -> {
            TreeNode<Object> node = e.getValue();

            Object nodeValue = node.getValue();

            if (nodeValue instanceof DatabaseJdbcTableNode) {
                DatabaseJdbcTableNode jdbcTableNode = (DatabaseJdbcTableNode) nodeValue;
                JdbcTableState value = jdbcTableNode.getValue();
                String fullName = value.getNameWithScheme();
                DataSourceEditorManager.getInstance(project).openEditor(jdbcTableNode.getDataSource(), jdbcTableNode.getDatabaseName(), fullName);
            }
        });

        MessageBusConnection connection = project.getMessageBus().connect(this);
        connection.subscribe(DataSourceListener.class, event -> myTree.refreshAll());

        connection.subscribe(DataSourceTransportListener.class, (dataSource, value) -> myTree.refreshAll());

        myTree.addExpandListener(e -> workspaceManager.setTreeState(UITreeState.createOn(myTree)));

        myTree.addCollapseListener(e -> workspaceManager.setTreeState(UITreeState.createOn(myTree)));

        UITreeState treeState = workspaceManager.getTreeState();
        if (treeState != null) {
            treeState.applyTo(myTree);
        }
        else {
            myTree.expandAll(2);
        }

        myRootPanel = ScrollableLayout.create(myTree, ScrollableLayoutOptions.builder().build());
    }

    public consulo.ui.Component getRootPanel() {
        return myRootPanel;
    }

    @Override
    public void uiDataSnapshot(DataSink sink) {
        sink.set(DataSourceKeys.TREE, myTree);
        sink.lazy(DataSourceKeys.DATASOURCE, () -> {
            TreeNode<Object> node = myTree.getSelectedNode();
            if (node != null) {
                Object lastUserObject = node.getValue();
                if (lastUserObject instanceof DatabaseSourceNode) {
                    return ((DatabaseSourceNode) lastUserObject).getValue();
                }
            }
            return null;
        });
    }

    @Override
    public void dispose() {
    }
}
