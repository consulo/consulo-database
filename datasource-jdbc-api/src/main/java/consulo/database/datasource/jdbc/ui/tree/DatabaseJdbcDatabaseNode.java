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

import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.jdbc.provider.impl.JdbcDatabaseState;
import consulo.database.datasource.model.DataSource;
import consulo.database.icon.DatabaseIconGroup;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentationData;

import javax.annotation.Nonnull;
import java.util.Collection;
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
		return List.of(new DatabaseJdbcTablesNode(myProject, getValue(), myDataSource));
	}

	@Override
	protected void update(PresentationData presentationData)
	{
		presentationData.setIcon(DatabaseIconGroup.nodesSchema());
		presentationData.addText(getValue().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
	}

	@Override
	public String toString()
	{
		return getValue().getName();
	}
}
