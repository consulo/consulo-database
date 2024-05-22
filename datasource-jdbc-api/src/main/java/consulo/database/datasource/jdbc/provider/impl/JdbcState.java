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
import java.util.Map;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class JdbcState implements PersistentStateComponent<JdbcState>
{
	private Map<String, JdbcDatabaseState> myDatabases = new TreeMap<>();

	@Nonnull
	public Map<String, JdbcDatabaseState> getDatabases()
	{
		return myDatabases;
	}

	public void setDatabases(Map<String, JdbcDatabaseState> databases)
	{
		myDatabases = databases;
	}

	public void addDatabase(@Nonnull String dbName, @Nonnull JdbcDatabaseState databaseState)
	{
		myDatabases.put(dbName, databaseState);
	}

	@Nullable
	@Override
	public JdbcState getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
