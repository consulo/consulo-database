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

import com.intellij.ide.DataManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.tree.TreeUtil;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceEvent;
import consulo.database.datasource.model.DataSourceListener;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.transport.DataSourceTransportListener;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.toolWindow.node.DatabaseSourceNode;
import consulo.disposer.Disposable;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseTreePanel implements Disposable
{
	private JPanel myRootPanel;

	public DatabaseTreePanel(@Nonnull Project project)
	{
		myRootPanel = new JPanel(new BorderLayout());

		DatabaseTreeStructure structure = new DatabaseTreeStructure(project);
		StructureTreeModel<DatabaseTreeStructure> treeModel = new StructureTreeModel<>(structure, this);
		Tree tree = new Tree(new AsyncTreeModel(treeModel, this));
		tree.setRootVisible(false);

		MessageBusConnection connection = project.getMessageBus().connect(this);
		connection.subscribe(DataSourceManager.TOPIC, new DataSourceListener()
		{
			@Override
			public void dataSourceEvent(DataSourceEvent event)
			{
				treeModel.invalidate(structure.getRootElement(), true);
			}
		});

		connection.subscribe(DataSourceTransportManager.TOPIC, new DataSourceTransportListener()
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

		TreeUtil.expand(tree, 2);

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
