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

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class JdbcTableColumState implements PersistentStateComponent<JdbcTableColumState>
{
	private String myName;
	private String myType;
	private int myJdbcType;

	private JdbcTableColumState()
	{
	}

	public JdbcTableColumState(String name, String type, int jdbcType)
	{
		myName = name;
		myType = type;
		myJdbcType = jdbcType;
	}

	public int getJdbcType()
	{
		return myJdbcType;
	}

	public String getName()
	{
		return myName;
	}

	public void setType(String type)
	{
		myType = type;
	}

	public String getType()
	{
		return myType;
	}

	public void setName(String name)
	{
		myName = name;
	}

	@Nullable
	@Override
	public JdbcTableColumState getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcTableColumState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
