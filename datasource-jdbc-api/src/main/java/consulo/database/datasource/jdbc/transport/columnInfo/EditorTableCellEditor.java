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

import com.intellij.ui.EditorTextField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

/**
 * @author VISTALL
 * @since 2020-09-01
 */
public class EditorTableCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private String myValue;

	public EditorTableCellEditor(String value)
	{
		myValue = value;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		EditorTextField field = new EditorTextField(myValue);
		field.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
		field.setFontInheritedFromLAF(false);
		return field;
	}

	@Override
	public Object getCellEditorValue()
	{
		return myValue;
	}
}
