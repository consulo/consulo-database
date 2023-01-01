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
import consulo.database.datasource.jdbc.provider.impl.JdbcPrimaryKeyState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableColumState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.icon.DatabaseIconGroup;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentationData;
import consulo.ui.image.ImageKey;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DatabaseJdbcColumnNode extends AbstractTreeNode<JdbcTableColumState>
{
	private final JdbcTableState myJdbcTableState;

	public DatabaseJdbcColumnNode(Project project, @Nonnull JdbcTableColumState value, JdbcTableState jdbcTableState)
	{
		super(project, value);
		myJdbcTableState = jdbcTableState;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		return Collections.emptyList();
	}

	@Override
	protected void update(PresentationData presentation)
	{
		ImageKey icon = DatabaseIconGroup.nodesColumn();
		List<JdbcPrimaryKeyState> primaryKeys = myJdbcTableState.getPrimaryKeys();
		JdbcTableColumState value = getValue();
		for(JdbcPrimaryKeyState primaryKey : primaryKeys)
		{
			if(value.getName().equals(primaryKey.getColumnName()))
			{
				icon = DatabaseIconGroup.nodesPrimary_key();
				break;
			}
		}

		presentation.setIcon(icon);
		presentation.addText(value.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		presentation.addText(" : " + value.getType(), SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);

		int size = value.getSize();
		if(size > 0)
		{
			presentation.addText(" (" + size + ")", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
		}

		String defaultValue = value.getDefaultValue();
		if(!StringUtil.isEmptyOrSpaces(defaultValue))
		{
			presentation.addText(" = " + defaultValue, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
		}
	}

	@Override
	public boolean isAlwaysLeaf()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return getValue().getName();
	}
}
