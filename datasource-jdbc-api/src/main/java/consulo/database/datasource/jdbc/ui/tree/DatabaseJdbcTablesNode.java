/*
 * Copyright 2013-2021 consulo.io
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

import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.jdbc.provider.impl.JdbcDatabaseState;
import consulo.database.datasource.jdbc.provider.impl.JdbcState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTablesState;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.icon.DatabaseIconGroup;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentationData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 08/12/2021
 */
public class DatabaseJdbcTablesNode extends AbstractTreeNode<JdbcDatabaseState>
{
	@Nonnull
	private final DataSource myDataSource;

	public DatabaseJdbcTablesNode(Project project, @Nonnull JdbcDatabaseState value, @Nonnull DataSource dataSource)
	{
		super(project, value);
		myDataSource = dataSource;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		List<AbstractTreeNode> nodes = new ArrayList<>();

		for(JdbcTableState table : getTables())
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

	private List<JdbcTableState> getTables()
	{
		List<JdbcTableState> tables;

		JdbcTablesState tablesState = getValue().getTablesState();

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

		return tables;
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
	protected void update(PresentationData presentation)
	{
		presentation.setIcon(DatabaseIconGroup.nodesFolder());
		JdbcDataSourceProvider provider = (JdbcDataSourceProvider) myDataSource.getProvider();
		int tables = 0;
		for(JdbcTableState state : getTables())
		{
			if(provider.isTableType(state.getType()))
			{
				tables ++;
			}
		}
		presentation.addText("tables", SimpleTextAttributes.REGULAR_ATTRIBUTES);
		presentation.addText(" " + tables, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
	}

	@Override
	public String toString()
	{
		return getValue().getName() + ".tables";
	}
}
