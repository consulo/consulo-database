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

package consulo.database.impl.configurable.editor;

import consulo.configurable.Configurable;
import consulo.configurable.ConfigurationException;
import consulo.dataContext.UiDataProvider;
import consulo.database.datasource.model.*;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.action.CopyDataSourceAction;
import consulo.database.impl.action.RemoveDataSourceAction;
import consulo.database.impl.configurable.editor.action.AddDataSourcePopupAction;
import consulo.database.impl.toolWindow.node.DatabaseSourceNode;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.*;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.dialog.DialogDescriptor;
import consulo.ui.ex.dialog.DialogValue;
import consulo.ui.ex.tree.TreeStructureWrappenModel;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.SplitLayoutPosition;
import consulo.ui.layout.TwoComponentSplitLayout;
import consulo.ui.util.ShowNotifier;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-08-01
 */
public class DatabaseSourcesDialogDescriptor extends DialogDescriptor {
    @Nonnull
    private final Project myProject;
    @Nonnull
    private final EditableDataSourceModel myEditableDataSourceModel;
    private final DataSource mySelectedDataSource;

    private Configurable mySelectedConfigurable;

    private DockLayout myLayout;

    public DatabaseSourcesDialogDescriptor(@Nonnull Project project,
                                           @Nonnull EditableDataSourceModel editableDataSourceModel,
                                           @Nullable DataSource selectedDataSource) {
        super(LocalizeValue.localizeTODO("Add/Edit DataSources"));
        myProject = project;
        myEditableDataSourceModel = editableDataSourceModel;
        mySelectedDataSource = selectedDataSource;
    }

    @Override
    public void onHandleValue(AnAction action, DialogValue value) {
        super.onHandleValue(action, value);

        if (value == DialogValue.OK_VALUE) {
            myEditableDataSourceModel.commit();
        }
        else {
            myEditableDataSourceModel.dispose();
        }
    }

    @RequiredUIAccess
    private void selectConfigurable(Disposable uiDisposable, @Nullable EditableDataSource dataSource, Runnable treeUpdater) {
        if (mySelectedConfigurable != null) {
            try {
                mySelectedConfigurable.apply();
            }
            catch (ConfigurationException ignored) {
            }

            mySelectedConfigurable = null;
        }

        UIAccess.current().execute(() -> {
            myLayout.removeAll();

            myLayout.forceRepaint();

            if (dataSource != null) {
                DataSourceConfigurable c = new DataSourceConfigurable(myProject, dataSource, treeUpdater);

                Component uiComponent = c.createUIComponent(uiDisposable);

                c.initialize();

                c.reset();

                mySelectedConfigurable = c;

                myLayout.center(uiComponent);
            }
        });
    }


    @Override
    public @Nullable Size2D getInitialSize() {
        return new Size2D(700, 500);
    }

    @RequiredUIAccess
    @Override
    public Component createCenterComponent(Disposable uiDisposable) {
        DataSourceTreeStructure structure = new DataSourceTreeStructure(myProject, myEditableDataSourceModel);

        TreeStructureWrappenModel<Object> wrapper = new TreeStructureWrappenModel<>(structure);

        consulo.ui.Tree<Object> tree = Tree.create(wrapper.getRootElement(), wrapper, uiDisposable);

        Runnable treeUpdater = tree::refreshAll;

        myEditableDataSourceModel.addListener(new DataSourceListener() {
            @Override
            public void dataSourceEvent(DataSourceEvent event) {
                tree.refreshAll().whenCompleteAsync((o, throwable) -> {
                    if (event.getAction() == DataSourceEvent.Action.ADD) {
                        selectInTree(tree, event.getDataSource());
                    }
                    else if (event.getAction() == DataSourceEvent.Action.REMOVE) {
                        List<? extends EditableDataSource> dataSources = myEditableDataSourceModel.getDataSources();
                        if (dataSources.isEmpty()) {
                            selectConfigurable(uiDisposable, null, treeUpdater);
                        }
                        else {
                            selectInTree(tree, dataSources.get(0));
                        }
                    }
                }, UIAccess.current());
            }
        });

        tree.addSelectListener(e -> {
            UIAccess.current().execute(() -> {
                Object lastUserObject = e.getValue().getValue();

                if (lastUserObject instanceof DatabaseSourceNode) {
                    DataSource value = ((DatabaseSourceNode) lastUserObject).getValue();
                    selectConfigurable(uiDisposable, (EditableDataSource) value, treeUpdater);
                }
                else {
                    selectConfigurable(uiDisposable, null, treeUpdater);
                }
            });
        });

        tree.expandAll();

        ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
        builder.add(new AddDataSourcePopupAction(myEditableDataSourceModel));
        builder.add(new RemoveDataSourceAction(myEditableDataSourceModel));
        builder.add(new CopyDataSourceAction(myEditableDataSourceModel));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DataSourceEditor", builder.build(), true);
        toolbar.setTargetUIComponent(tree);

        DockLayout panel = DockLayout.create();

        Component uiComponent = toolbar.getUIComponent();

        panel.top(uiComponent);
        panel.center(tree);

        myLayout = DockLayout.create();

        panel.putUserData(UiDataProvider.KEY, sink -> {
            sink.lazy(DataSourceKeys.DATASOURCE, () -> {
                TreeNode<Object> node = tree.getSelectedNode();
                Object lastUserObject = node == null ? null : node.getValue();
                if (lastUserObject instanceof DatabaseSourceNode) {
                    return ((DatabaseSourceNode) lastUserObject).getValue();
                }
                return null;
            });
        });

        if (mySelectedDataSource != null) {
            DataSource dataSource = myEditableDataSourceModel.findDataSource(mySelectedDataSource.getId());

            ShowNotifier.once(panel, () -> {
                selectInTree(tree, dataSource);
            });
        }

        TwoComponentSplitLayout layout = TwoComponentSplitLayout.create(SplitLayoutPosition.HORIZONTAL);
        layout.setProportion(25);
        layout.setFirstComponent(panel);
        layout.setSecondComponent(myLayout);
        return layout;
    }

    private void selectInTree(Tree<Object> tree, DataSource dataSource) {
        TreeNode<Object> treeRootNode = tree.getRootNode();
        treeRootNode.findChildDeep(o -> o instanceof DatabaseSourceNode && ((DatabaseSourceNode) o).getValue().equals(dataSource))
            .whenCompleteAsync((treeNode, throwable) -> {
                if (treeNode != null) {
                    tree.select(treeNode);
                }
            }, UIAccess.current());
    }
}
