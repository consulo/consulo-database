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

package consulo.database.datasource.jdbc.transport.columnInfo;

import com.intellij.util.ui.ColumnInfo;
import consulo.database.jdbc.rt.shared.JdbcQueryRow;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-30
 */
public abstract class BaseColumnInfo<T> extends ColumnInfo<JdbcQueryRow, T>
{
	protected final int myIndex;
	private String myPreferedSize;

	public BaseColumnInfo(int index, String name, String preferedSize)
	{
		super(name);
		myIndex = index;
		myPreferedSize = preferedSize;
	}

	@Nullable
	@Override
	public String getPreferredStringValue()
	{
		// two chapters as border
		return myPreferedSize + "ww";
	}
}
