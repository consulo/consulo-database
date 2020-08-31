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

package consulo.database.datasource.jdbc.provider.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class JdbcTablesState implements PersistentStateComponent<JdbcTablesState>
{
	private List<JdbcTableState> myTables = new ArrayList<>();

	@Nonnull
	public List<JdbcTableState> getTables()
	{
		return myTables;
	}

	@Nullable
	public JdbcTableState findTable(@Nonnull String tableWithScheme)
	{
		for(JdbcTableState state : myTables)
		{
			if(tableWithScheme.equals(state.getNameWithScheme()))
			{
				return state;
			}
		}
		return null;
	}

	public void setTables(List<JdbcTableState> tables)
	{
		myTables = tables;
	}

	public void addTable(JdbcTableState state)
	{
		myTables.add(state);
	}

	@Nullable
	@Override
	public JdbcTablesState getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcTablesState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
