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
import consulo.dataContext.DataProvider;
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
import consulo.util.dataholder.Key;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
public class DatabaseTreePanel extends JPanel implements Disposable, DataProvider
{
	private static final Logger LOG = Logger.getInstance(DatabaseTreePanel.class);

	private JPanel myRootPanel;
	private final Tree myTree;

	public DatabaseTreePanel(@Nonnull Project project)
	{
		myRootPanel = new JPanel(new BorderLayout());
		DataSourceWorkspaceManager workspaceManager = DataSourceWorkspaceManager.getInstance(project);

		DatabaseTreeStructure structure = new DatabaseTreeStructure(project);
		StructureTreeModel<DatabaseTreeStructure> treeModel = new StructureTreeModel<>(structure, this);
		myTree = new Tree(new AsyncTreeModel(treeModel, this))
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

		myTree.addTreeExpansionListener(new TreeExpansionListener()
		{
			@Override
			public void treeExpanded(TreeExpansionEvent treeExpansionEvent)
			{
				workspaceManager.setTreeState(TreeState.createOn(myTree));
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent treeExpansionEvent)
			{
				workspaceManager.setTreeState(TreeState.createOn(myTree));
			}
		});
		myTree.setRootVisible(false);
		new DoubleClickListener()
		{
			@Override
			protected boolean onDoubleClick(MouseEvent event)
			{
				TreePath path = TreeUtil.getSelectedPathIfOne(myTree);
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
		}.installOn(myTree);

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

		TreeState treeState = workspaceManager.getTreeState();
		if(treeState != null)
		{
			treeState.applyTo(myTree);
		}
		else
		{
			TreeUtil.expand(myTree, 2);
		}

		myRootPanel.add(ScrollPaneFactory.createScrollPane(myTree, true));
	}

	public JPanel getRootPanel()
	{
		return myRootPanel;
	}

	@Nullable
	@Override
	public Object getData(@Nonnull Key<?> key)
	{
		if(key == DataSourceKeys.TREE)
		{
			return myTree;
		}
		else if(key == DataSourceKeys.DATASOURCE)
		{
			TreePath path = TreeUtil.getSelectedPathIfOne(myTree);
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
	}

	@Override
	public void dispose()
	{

	}
}
