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

import consulo.database.datasource.jdbc.transport.DefaultJdbcDataSourceTransport;
import consulo.database.icon.DatabaseIconGroup;
import consulo.database.jdbc.rt.shared.JdbcQueryRow;
import consulo.disposer.Disposable;
import consulo.language.editor.ui.awt.EditorAWTUtil;
import consulo.ui.ex.awt.CellRendererPanel;
import consulo.ui.ex.awt.ColumnInfo;
import consulo.ui.ex.awt.JBUI;
import consulo.ui.ex.awt.SimpleColoredComponent;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.ui.image.Image;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author VISTALL
 * @since 2020-08-30
 */
public abstract class BaseColumnInfo<T> extends ColumnInfo<JdbcQueryRow, T>
{
	public static class MyCellRendererPanel extends CellRendererPanel implements TableCellRenderer
	{
		private SimpleColoredComponent mySimpleColoredComponent;

		public MyCellRendererPanel(String text, boolean index)
		{
			mySimpleColoredComponent = new SimpleColoredComponent();
			mySimpleColoredComponent.append(text);
			mySimpleColoredComponent.setFont(BaseColumnInfo.getFont());
			mySimpleColoredComponent.setIpad(JBUI.insets(3, 2, 2, 2));
			mySimpleColoredComponent.setOpaque(index);
			if(index)
			{
				mySimpleColoredComponent.setTextAlign(SwingConstants.CENTER);
			}

			add(mySimpleColoredComponent, BorderLayout.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return this;
		}
	}

	public static Font getFont()
	{
		return EditorAWTUtil.getEditorFont();
	}

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

	@Nullable
	@Override
	public Icon getIcon()
	{
		return TargetAWT.to(DatabaseIconGroup.nodesColumn());
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
		return new MyCellRendererPanel(String.valueOf(DefaultJdbcDataSourceTransport.getValue(jdbcQueryRow, myIndex)), false);
	}

	@Nullable
	@Override
	public String getPreferredStringValue()
	{
		// two chapters as border and image and offsets
		return myPreferedSize + "ww" + Image.DEFAULT_ICON_SIZE * 2;
	}
}
