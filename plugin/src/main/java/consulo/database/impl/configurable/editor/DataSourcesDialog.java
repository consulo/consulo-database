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

package consulo.database.impl.configurable.editor;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.tree.TreeVisitor;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.ui.update.UiNotifyConnector;
import consulo.application.ui.WholeWestDialogWrapper;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.*;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.action.CopyDataSourceAction;
import consulo.database.impl.action.RemoveDataSourceAction;
import consulo.database.impl.configurable.editor.action.AddDataSourcePopupAction;
import consulo.database.impl.toolWindow.node.DatabaseSourceNode;
import consulo.options.ConfigurableUIMigrationUtil;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.decorator.SwingUIDecorator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourcesDialog extends WholeWestDialogWrapper
{
	@Nonnull
	private final Project myProject;
	@Nullable
	private final DataSource mySelectedDataSource;

	private JPanel myConfigurablePanel;

	private Configurable mySelectedConfigurable;

	private final EditableDataSourceModel myEditableDataSourceModel;

	public DataSourcesDialog(@Nonnull Project project, @Nullable DataSource selectedDataSource)
	{
		this(project, DataSourceManager.getInstance(project).createEditableModel(), selectedDataSource);
	}

	public DataSourcesDialog(@Nonnull Project project, @Nonnull EditableDataSourceModel editableDataSourceModel, @Nullable DataSource selectedDataSource)
	{
		super(project);
		myProject = project;
		mySelectedDataSource = selectedDataSource;
		myEditableDataSourceModel = editableDataSourceModel;

		setTitle("Add/Edit DataSources");

		init();
	}

	@Nullable
	@Override
	protected String getDimensionServiceKey()
	{
		return DataSourcesDialog.class.getName();
	}

	@Override
	public Dimension getDefaultSize()
	{
		return new Dimension(700, 500);
	}

	@RequiredUIAccess
	@Nonnull
	@Override
	public Couple<JComponent> createSplitterComponents(JPanel rootPanel)
	{
		DataSourceTreeStructure structure = new DataSourceTreeStructure(myProject, myEditableDataSourceModel);

		StructureTreeModel<DataSourceTreeStructure> structureModel = new StructureTreeModel<>(structure, myProject);
		AsyncTreeModel model = new AsyncTreeModel(structureModel, myProject);
		Tree tree = new Tree(model);

		Runnable treeUpdater = () ->
		{
			structureModel.invalidate(structure.getRootElement(), true);
		};

		myEditableDataSourceModel.addListener(new DataSourceListener()
		{
			@Override
			public void dataSourceEvent(DataSourceEvent event)
			{
				structureModel.invalidate().onSuccess(o ->
				{
					if(event.getAction() == DataSourceEvent.Action.ADD)
					{
						selectInTree(tree, event.getDataSource(), model);
					}
					else if(event.getAction() == DataSourceEvent.Action.REMOVE)
					{
						List<? extends EditableDataSource> dataSources = myEditableDataSourceModel.getDataSources();
						if(dataSources.isEmpty())
						{
							selectConfigurable(null, treeUpdater);
						}
						else
						{
							selectInTree(tree, dataSources.get(0), model);
						}
					}
				});
			}
		});

		tree.addTreeSelectionListener(e ->
		{
			SwingUtilities.invokeLater(() ->
			{
				Object lastUserObject = TreeUtil.getLastUserObject(e.getNewLeadSelectionPath());

				if(lastUserObject instanceof DatabaseSourceNode)
				{
					DataSource value = ((DatabaseSourceNode) lastUserObject).getValue();
					selectConfigurable((EditableDataSource) value, treeUpdater);
				}
				else
				{
					selectConfigurable(null, treeUpdater);
				}
			});
		});

		tree.setOpaque(false);
		tree.setRootVisible(false);
		TreeUtil.expandAll(tree);

		ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
		builder.add(new AddDataSourcePopupAction(myEditableDataSourceModel));
		builder.add(new RemoveDataSourceAction(myEditableDataSourceModel));
		builder.add(new CopyDataSourceAction(myEditableDataSourceModel));

		ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DataSourceEditor", builder.build(), true);
		toolbar.setTargetComponent(tree);

		BorderLayoutPanel panel = new BorderLayoutPanel();
		panel.setBackground(SwingUIDecorator.get(SwingUIDecorator::getSidebarColor));

		JComponent component = toolbar.getComponent();
		component.setOpaque(false);
		component.setBackground(SwingUIDecorator.get(SwingUIDecorator::getSidebarColor));
		panel.addToTop(component);
		panel.addToCenter(tree);

		myConfigurablePanel = new JPanel(new BorderLayout());

		DataManager.registerDataProvider(panel, dataId ->
		{
			if(dataId == DataSourceKeys.DATASOURCE)
			{
				TreePath path = TreeUtil.getSelectedPathIfOne(tree);
				if(path != null)
				{
					Object lastUserObject = TreeUtil.getLastUserObject(path);
					if(lastUserObject instanceof DatabaseSourceNode)
					{
						return ((DatabaseSourceNode) lastUserObject).getValue();
					}
				}
			}

			return null;
		});

		if(mySelectedDataSource != null)
		{
			DataSource dataSource = myEditableDataSourceModel.findDataSource(mySelectedDataSource.getId());

			UiNotifyConnector.doWhenFirstShown(panel, () ->
			{
				selectInTree(tree, dataSource, model);
			});
		}
		return Couple.of(panel, myConfigurablePanel);
	}

	private void selectInTree(Tree tree, DataSource dataSource, AsyncTreeModel model)
	{
		model.accept(new TreeVisitor()
		{
			@Nonnull
			@Override
			public Action visit(@Nonnull TreePath treePath)
			{
				Object maybeNode = treePath.getLastPathComponent();
				if(maybeNode instanceof DefaultMutableTreeNode)
				{
					Object userObject = ((DefaultMutableTreeNode) maybeNode).getUserObject();

					if(userObject instanceof DatabaseSourceNode && ((DatabaseSourceNode) userObject).getValue().equals(dataSource))
					{
						return Action.INTERRUPT;
					}
				}
				return Action.CONTINUE;
			}
		}, true).onSuccess(treePath -> {
			if(treePath != null)
			{
				TreeUtil.selectPath(tree, treePath);
			}
		});
	}

	@RequiredUIAccess
	private void selectConfigurable(@Nullable EditableDataSource dataSource, Runnable treeUpdater)
	{
		if(mySelectedConfigurable != null)
		{
			try
			{
				mySelectedConfigurable.apply();
			}
			catch(ConfigurationException ignored)
			{
			}

			mySelectedConfigurable = null;
		}

		SwingUtilities.invokeLater(() ->
		{
			myConfigurablePanel.removeAll();

			myConfigurablePanel.revalidate();

			myConfigurablePanel.repaint();

			if(dataSource != null)
			{
				DataSourceConfigurable c = new DataSourceConfigurable(myProject, dataSource, treeUpdater);

				JComponent component = ConfigurableUIMigrationUtil.createComponent(c, getDisposable());

				c.reset();

				mySelectedConfigurable = c;

				myConfigurablePanel.add(component, BorderLayout.CENTER);
			}
		});
	}

	@Override
	@RequiredUIAccess
	protected void doOKAction()
	{
		if(mySelectedConfigurable != null)
		{
			try
			{
				mySelectedConfigurable.apply();
			}
			catch(ConfigurationException ignored)
			{
			}
		}

		myEditableDataSourceModel.commit();

		super.doOKAction();
	}

	@Override
	public void doCancelAction()
	{
		myEditableDataSourceModel.dispose();

		super.doCancelAction();
	}

	@Nullable
	@Override
	protected Border createContentPaneBorder()
	{
		return JBUI.Borders.empty();
	}

	@Nullable
	@Override
	protected JComponent createSouthPanel()
	{
		JComponent southPanel = super.createSouthPanel();
		if(southPanel != null)
		{
			southPanel.setBorder(JBUI.Borders.empty(ourDefaultBorderInsets));
			return JBUI.Panels.simplePanel(southPanel);
		}
		return null;
	}
}
