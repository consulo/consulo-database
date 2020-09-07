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

package consulo.database.datasource.jdbc.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.jdbc.provider.impl.JdbcDatabaseState;
import consulo.database.datasource.jdbc.provider.impl.JdbcState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTablesState;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransportManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class DatabaseJdbcDatabaseNode extends AbstractTreeNode<JdbcDatabaseState>
{
	protected final DataSource myDataSource;

	public DatabaseJdbcDatabaseNode(Project project, DataSource dataSource, JdbcDatabaseState state)
	{
		super(project, state);
		myDataSource = dataSource;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		JdbcTablesState tablesState = getValue().getTablesState();

		List<JdbcTableState> tables;

		// database selected inside interface
		if(tablesState == null)
		{
			JdbcState dataState = DataSourceTransportManager.getInstance(myProject).getDataState(myDataSource);
			if(dataState == null)
			{
				return Collections.emptyList();
			}

			JdbcDatabaseState databaseState = dataState.getDatabases().get(getValue().getName());
			if(databaseState == null || databaseState.getTablesState() == null)
			{
				return Collections.emptyList();
			}

			tables = databaseState.getTablesState().getTables();
		}
		else
		{
			tables = tablesState.getTables();
		}

		if(tables.isEmpty())
		{
			return Collections.emptyList();
		}

		List<AbstractTreeNode> nodes = new ArrayList<>();

		for(JdbcTableState table : tables)
		{
			AbstractTreeNode tableNode = createTableNode(table);
			if(tableNode == null)
			{
				continue;
			}
			nodes.add(tableNode);
		}
		return nodes;
	}

	@Nullable
	protected AbstractTreeNode createTableNode(JdbcTableState table)
	{
		JdbcDataSourceProvider provider = (JdbcDataSourceProvider) myDataSource.getProvider();
		if(!provider.isTableType(table.getType()))
		{
			return null;
		}
		return new DatabaseJdbcTableNode(myProject, myDataSource, getValue().getName(), table);
	}

	@Override
	protected void update(PresentationData presentationData)
	{
		presentationData.setIcon(AllIcons.Nodes.DataSource);
		presentationData.addText(getValue().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
	}

	@Override
	public String toString()
	{
		return getValue().getName();
	}
}
