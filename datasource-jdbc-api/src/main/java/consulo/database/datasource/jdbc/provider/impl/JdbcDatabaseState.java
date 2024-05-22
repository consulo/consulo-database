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

import consulo.component.persist.PersistentStateComponent;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class JdbcDatabaseState implements PersistentStateComponent<JdbcDatabaseState>
{
	private JdbcTablesState myTablesState;

	private String myName;

	public JdbcDatabaseState()
	{
		myTablesState = new JdbcTablesState();
	}

	public JdbcDatabaseState(@Nonnull String name, @Nonnull JdbcTablesState tables)
	{
		myName =  name;
		myTablesState = tables;
	}

	public String getName()
	{
		return myName;
	}

	public void setName(String name)
	{
		myName = name;
	}

	public JdbcTablesState getTablesState()
	{
		return myTablesState;
	}

	public void setTablesState(JdbcTablesState tablesState)
	{
		myTablesState = tablesState;
	}

	@Nullable
	@Override
	public JdbcDatabaseState getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcDatabaseState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
