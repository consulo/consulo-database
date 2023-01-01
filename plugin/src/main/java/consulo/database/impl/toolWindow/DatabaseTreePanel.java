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
import consulo.dataContext.DataManager;
import consulo.database.datasource.editor.DataSourceEditorManager;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.jdbc.ui.tree.DatabaseJdbcTableNode;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceEvent;
import consulo.database.datasource.model.DataSourceListener;
import consulo.database.datasource.transport.DataSourceTransportListener;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.DataSourceWorkspaceManager;
import consulo.database.impl.toolWindow.node.DatabaseSourceNode;
import consulo.disposer.Disposable;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.ex.awt.ScrollPaneFactory;
import consulo.ui.ex.awt.event.DoubleClickListener;
import consulo.ui.ex.awt.tree.*;
import consulo.ui.ex.tree.NodeDescriptor;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseTreePanel implements Disposable
{
	private static final Logger LOG = Logger.getInstance(DatabaseTreePanel.class);

	private JPanel myRootPanel;

	public DatabaseTreePanel(@Nonnull Project project)
	{
		myRootPanel = new JPanel(new BorderLayout());
		DataSourceWorkspaceManager workspaceManager = DataSourceWorkspaceManager.getInstance(project);

		DatabaseTreeStructure structure = new DatabaseTreeStructure(project);
		StructureTreeModel<DatabaseTreeStructure> treeModel = new StructureTreeModel<>(structure, this);
		Tree tree = new Tree(new AsyncTreeModel(treeModel, this))
		{
			@Override
			public final int getToggleClickCount()
			{
				int count = super.getToggleClickCount();
				TreePath path = getSelectionPath();
				if(path != null)
				{
					NodeDescriptor descriptor = TreeUtil.getLastUserObject(NodeDescriptor.class, path);
					if(descriptor != null && !descriptor.expandOnDoubleClick())
					{
						LOG.debug("getToggleClickCount: -1 for ", descriptor.getClass().getName());
						return -1;
					}
				}
				return count;
			}
		};

		tree.addTreeExpansionListener(new TreeExpansionListener()
		{
			@Override
			public void treeExpanded(TreeExpansionEvent treeExpansionEvent)
			{
				workspaceManager.setTreeState(TreeState.createOn(tree));
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent treeExpansionEvent)
			{
				workspaceManager.setTreeState(TreeState.createOn(tree));
			}
		});
		tree.setRootVisible(false);
		new DoubleClickListener()
		{
			@Override
			protected boolean onDoubleClick(MouseEvent event)
			{
				TreePath path = TreeUtil.getSelectedPathIfOne(tree);
				if(path != null)
				{
					Object node = TreeUtil.getLastUserObject(path);
					if(node instanceof DatabaseJdbcTableNode)
					{
						DatabaseJdbcTableNode jdbcTableNode = (DatabaseJdbcTableNode) node;
						JdbcTableState value = jdbcTableNode.getValue();
						String fullName = value.getNameWithScheme();
						DataSourceEditorManager.getInstance(project).openEditor(jdbcTableNode.getDataSource(), jdbcTableNode.getDatabaseName(), fullName);
						return true;
					}
				}

				return false;
			}
		}.installOn(tree);

		MessageBusConnection connection = project.getMessageBus().connect(this);
		connection.subscribe(DataSourceListener.class, new DataSourceListener()
		{
			@Override
			public void dataSourceEvent(DataSourceEvent event)
			{
				treeModel.invalidate(structure.getRootElement(), true);
			}
		});

		connection.subscribe(DataSourceTransportListener.class, new DataSourceTransportListener()
		{
			@Override
			public void dataUpdated(@Nonnull DataSource dataSource, @Nonnull Object value)
			{
				treeModel.invalidate(structure.getRootElement(), true);
			}
		});

		DataManager.registerDataProvider(myRootPanel, key ->
		{
			if(key == DataSourceKeys.TREE)
			{
				return tree;
			}
			else if(key == DataSourceKeys.DATASOURCE)
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

		TreeState treeState = workspaceManager.getTreeState();
		if(treeState != null)
		{
			treeState.applyTo(tree);
		}
		else
		{
			TreeUtil.expand(tree, 2);
		}

		myRootPanel.add(ScrollPaneFactory.createScrollPane(tree, true));
	}

	public JPanel getRootPanel()
	{
		return myRootPanel;
	}

	@Override
	public void dispose()
	{

	}
}
