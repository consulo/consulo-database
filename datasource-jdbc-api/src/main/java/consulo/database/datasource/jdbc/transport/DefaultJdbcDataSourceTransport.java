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

package consulo.database.datasource.jdbc.transport;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.jdbc.provider.impl.*;
import consulo.database.datasource.jdbc.transport.columnInfo.BaseColumnInfo;
import consulo.database.datasource.jdbc.transport.columnInfo.IntColumnInfo;
import consulo.database.datasource.jdbc.transport.columnInfo.StringColumnInfo;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.impl.editor.ui.TableViewWithHScrolling;
import consulo.database.jdbc.rt.shared.*;
import consulo.logging.Logger;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class DefaultJdbcDataSourceTransport implements DataSourceTransport<JdbcState>
{
	private static final Logger LOG = Logger.getInstance(DefaultJdbcDataSourceTransport.class);

	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return dataSource.getProvider() instanceof JdbcDataSourceProvider;
	}

	private <T> void safeCall(@Nonnull ProgressIndicator indicator, @Nonnull DataSource dataSource, @Nonnull AsyncResult<T> result, @Nonnull ThrowableConsumer<JdbcSession, Throwable> sessionConsumer)
	{
		try (JdbcSession session = new JdbcSession(indicator, dataSource))
		{
			sessionConsumer.consume(session);
		}
		catch(ProcessCanceledException e)
		{
			result.rejectWithThrowable(e);
		}
		catch(Throwable e)
		{
			LOG.warn(e);

			result.rejectWithThrowable(e);
		}
	}

	@Override
	public void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result)
	{
		safeCall(indicator, dataSource, result, session ->
		{
			Boolean valid = session.execute(JdbcExecutor.Client::testConnection);

			if(valid)
			{
				result.setDone();
			}
			else
			{
				result.rejectWithThrowable(new Error("Failed"));
			}
		});
	}

	@Override
	public void loadInitialData(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<JdbcState> result)
	{
		String databaseName = StringUtil.nullize(dataSource.getProperties().get(GenericPropertyKeys.DATABASE_NAME));

		safeCall(indicator, dataSource, result, session ->
		{
			JdbcState state = new JdbcState();

			if(databaseName == null)
			{
				List<String> databases = session.execute(JdbcExecutor.Client::listDatabases);

				for(String database : databases)
				{
					indicator.setText("Processsing '" + dataSource.getName() + ":" + database + "'");

					List<JdbcTable> jdbcTables = session.execute(client -> client.listTables(database));

					state.addDatabase(database, new JdbcDatabaseState(database, convertToTables(jdbcTables)));
				}
			}
			else
			{
				indicator.setText("Processsing '" + dataSource.getName() + ":" + databaseName + "'");

				List<JdbcTable> jdbcTables = session.execute(client -> client.listTables(databaseName));

				state.addDatabase(databaseName, new JdbcDatabaseState(databaseName, convertToTables(jdbcTables)));
			}

			result.setDone(state);
		});
	}

	@Override
	public void fetchData(@Nonnull ProgressIndicator indicator,
						  @Nonnull Project project,
						  @Nonnull DataSource dataSource,
						  @Nonnull String databaseName,
						  @Nonnull String childId,
						  @Nonnull AsyncResult<Object> result)
	{
		safeCall(indicator, dataSource, result, session ->
		{
			session.execute(client -> {
				client.setDatabase(databaseName);
				return null;
			});

			String countQuery = "SELECT COUNT(*) FROM " + childId;

			JdbcQueryResult jdbcQueryResult = session.execute(client -> client.runQuery(countQuery, Collections.emptyList()));

			List<JdbcQueryRow> rows = jdbcQueryResult.getRows();

			JdbcQueryRow row = rows.get(0);

			Number value = (Number) getValue(row, 0);

			long rowsCount = value.longValue();

			// TODO pagging
			String query = "SELECT * FROM " + childId;

			JdbcQueryResult queryResult = session.execute(client -> client.runQuery(query, Collections.emptyList()));

			result.setDone(queryResult);
		});
	}

	@RequiredUIAccess
	@Override
	public void fetchDataEnded(@Nonnull ProgressIndicator indicator,
							   @Nonnull Project project,
							   @Nonnull DataSource dataSource,
							   @Nonnull String dbName,
							   @Nonnull String childId,
							   @Nonnull Object data,
							   @Nonnull Consumer<JComponent> setter)
	{
		JdbcQueryResult queryResult = (JdbcQueryResult) data;

		JdbcState dataState = DataSourceTransportManager.getInstance(project).getDataState(dataSource);

		JdbcTableState tableState = null;
		JdbcDatabaseState databaseState = dataState == null ? null : dataState.getDatabases().get(dbName);
		if(databaseState != null)
		{
			tableState = databaseState.getTablesState().findTable(childId);
		}

		List<ColumnInfo<JdbcQueryRow, ?>> list = new ArrayList<>();
		int o = 0;
		for(String col : queryResult.getColumns())
		{
			final int index = o++;

			String max = col;
			for(JdbcQueryRow row : queryResult.getRows())
			{
				String item = String.valueOf(getValue(row, index));

				if(max == null || item.length() > max.length())
				{
					max = item;
				}
			}

			list.add(createColumn(index, col, max, tableState));
		}

		TableViewWithHScrolling<JdbcQueryRow> tableView = new TableViewWithHScrolling<>(new ListTableModel<>(list.toArray(new ColumnInfo[0]), queryResult.getRows()));
		tableView.setHorizontalScrollEnabled(true);
		tableView.setFont(UIUtil.getLabelFont(UIUtil.FontSize.BIGGER));
		tableView.setRowHeight(JBUI.scale(24));

		setter.accept(ScrollPaneFactory.createScrollPane(tableView, true));
	}

	private static BaseColumnInfo<?> createColumn(int index, String name, String preferedSize, JdbcTableState tableState)
	{
		if(tableState != null)
		{
			JdbcTableColumState column = tableState.findColumn(name);
			if(column != null)
			{
				switch(column.getJdbcType())
				{
					case Types.INTEGER:
					case Types.SMALLINT:
					case Types.TINYINT:
						return new IntColumnInfo(index, name, preferedSize);
				}
			}
		}
		return new StringColumnInfo(index, name, preferedSize);
	}

	public static Object getValue(@Nonnull JdbcQueryRow row, int index)
	{
		List<JdbcValue> values = row.getValues();

		JdbcValue value = values.get(index);

		JdbcValueType type = value.getType();
		switch(type)
		{
			case _int:
				return value.getIntValue();
			case _string:
				return value.getStringValue();
			case _bool:
				return value.isBoolValue();
			case _long:
				return value.getLongValue();
			default:
				throw new UnsupportedOperationException(type.name());
		}
	}

	@Nonnull
	private static JdbcTablesState convertToTables(List<JdbcTable> jdbcTables)
	{
		JdbcTablesState state = new JdbcTablesState();
		for(JdbcTable jdbcTable : jdbcTables)
		{
			JdbcTableState tableState = new JdbcTableState();
			tableState.setName(jdbcTable.getName());
			tableState.setType(jdbcTable.getType());

			for(JdbcColum colum : jdbcTable.getColums())
			{
				tableState.addColumn(new JdbcTableColumState(colum.getName(), colum.getType(), colum.getJdbcType()));
			}
			state.addTable(tableState);
		}
		return state;
	}

	@Nonnull
	@Override
	public Class<JdbcState> getStateClass()
	{
		return JdbcState.class;
	}

	@Override
	public int getStateVersion()
	{
		return 2;
	}
}