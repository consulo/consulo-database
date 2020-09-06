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

import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.ui.EditorTextFieldCellRenderer;
import com.intellij.util.ui.ColumnInfo;
import consulo.database.datasource.jdbc.transport.DefaultJdbcDataSourceTransport;
import consulo.database.jdbc.rt.shared.JdbcQueryRow;
import consulo.disposer.Disposable;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * @author VISTALL
 * @since 2020-08-30
 */
public abstract class BaseColumnInfo<T> extends ColumnInfo<JdbcQueryRow, T>
{
	protected final int myIndex;
	private String myPreferedSize;
	private final Disposable myParent;

	public BaseColumnInfo(int index, String name, String preferedSize, Disposable parent)
	{
		super(name);
		myIndex = index;
		myPreferedSize = preferedSize;
		myParent = parent;
	}

	@Override
	public boolean isCellEditable(JdbcQueryRow row)
	{
		return true;
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(JdbcQueryRow o)
	{
		return new EditorTableCellEditor(String.valueOf(DefaultJdbcDataSourceTransport.getValue(o, myIndex)));
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(JdbcQueryRow jdbcQueryRow)
	{
		return new EditorTextFieldCellRenderer(null, PlainTextFileType.INSTANCE, false, myParent)
		{
			@Override
			protected String getText(JTable table, Object value, int row, int column)
			{
				return String.valueOf(DefaultJdbcDataSourceTransport.getValue(jdbcQueryRow, myIndex));
			}
		};
	}

	@Nullable
	@Override
	public String getPreferredStringValue()
	{
		// two chapters as border
		return myPreferedSize + "ww";
	}
}
