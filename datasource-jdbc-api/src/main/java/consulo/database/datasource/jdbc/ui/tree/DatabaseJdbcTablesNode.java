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
import consulo.database.datasource.jdbc.provider.impl.JdbcDatabaseState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTablesState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public class DatabaseJdbcTablesNode extends AbstractTreeNode<JdbcDatabaseState>
{
	public DatabaseJdbcTablesNode(Project project, JdbcDatabaseState jdbcDatabaseState)
	{
		super(project, jdbcDatabaseState);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		JdbcTablesState tablesState = getValue().getTablesState();
		if(tablesState == null)
		{
			return Collections.emptyList();
		}

		List<AbstractTreeNode> nodes = new ArrayList<>();

		List<JdbcTableState> tables = tablesState.getTables();

		for(JdbcTableState table : tables)
		{
			nodes.add(new DatabaseJdbcTableNode(myProject, table));
		}
		return nodes;
	}

	@Override
	protected void update(PresentationData presentation)
	{
		presentation.setIcon(AllIcons.Nodes.DataSource);
		presentation.addText("Tables", SimpleTextAttributes.REGULAR_ATTRIBUTES);
	}
}
