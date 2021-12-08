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

package consulo.database.datasource.jdbc.provider.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 08/12/2021
 */
public class JdbcPrimaryKeyState implements PersistentStateComponent<JdbcPrimaryKeyState>
{
	private String myColumnName;
	private short myKeySeq;
	private String myPkName;

	public JdbcPrimaryKeyState()
	{
	}

	public JdbcPrimaryKeyState(String columnName, short keySeq, String pkName)
	{
		myColumnName = columnName;
		myKeySeq = keySeq;
		myPkName = pkName;
	}

	public String getColumnName()
	{
		return myColumnName;
	}

	public void setColumnName(String columnName)
	{
		myColumnName = columnName;
	}

	public short getKeySeq()
	{
		return myKeySeq;
	}

	public void setKeySeq(short keySeq)
	{
		myKeySeq = keySeq;
	}

	public String getPkName()
	{
		return myPkName;
	}

	public void setPkName(String pkName)
	{
		myPkName = pkName;
	}

	@Nullable
	@Override
	public JdbcPrimaryKeyState getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcPrimaryKeyState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
