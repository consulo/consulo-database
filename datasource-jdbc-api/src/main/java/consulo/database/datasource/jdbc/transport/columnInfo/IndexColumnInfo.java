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

package consulo.database.datasource.jdbc.transport.columnInfo;

import com.intellij.util.ui.ColumnInfo;
import consulo.database.jdbc.rt.shared.JdbcQueryResult;
import consulo.database.jdbc.rt.shared.JdbcQueryRow;

import javax.annotation.Nullable;
import javax.swing.table.TableCellRenderer;

/**
 * @author VISTALL
 * @since 09/12/2021
 */
public class IndexColumnInfo extends ColumnInfo<JdbcQueryRow, Long>
{
	private final JdbcQueryResult myQueryResult;

	public IndexColumnInfo(JdbcQueryResult queryResult)
	{
		super("");
		myQueryResult = queryResult;
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(JdbcQueryRow jdbcQueryRow)
	{
		return new BaseColumnInfo.MyCellRendererPanel(String.valueOf(valueOf(jdbcQueryRow)), true);
	}

	@Nullable
	@Override
	public String getPreferredStringValue()
	{
		return "xxx";
	}

	@Nullable
	@Override
	public Long valueOf(JdbcQueryRow row)
	{
		return (long) myQueryResult.getRows().indexOf(row);
	}
}
