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
import consulo.database.datasource.jdbc.provider.impl.JdbcTableColumState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.icon.DatabaseIconGroup;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentationData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DatabaseJdbcColumnsNode extends AbstractTreeNode<JdbcTableState>
{
	public DatabaseJdbcColumnsNode(Project project, @Nonnull JdbcTableState value)
	{
		super(project, value);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		List<AbstractTreeNode> columns = new ArrayList<>();
		for(JdbcTableColumState state : getValue().getColumns())
		{
			columns.add(new DatabaseJdbcColumnNode(myProject, state, getValue()));
		}
		return columns;
	}

	@Override
	protected void update(PresentationData presentation)
	{
		presentation.setIcon(DatabaseIconGroup.nodesFolder());
		presentation.addText("columns", SimpleTextAttributes.REGULAR_ATTRIBUTES);
		presentation.addText(" " + getValue().getColumns().size(), SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
	}
}
