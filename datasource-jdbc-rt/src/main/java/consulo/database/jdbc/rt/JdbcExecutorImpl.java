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

package consulo.database.jdbc.rt;

import consulo.database.jdbc.rt.shared.*;
import org.apache.thrift.TException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class JdbcExecutorImpl implements JdbcExecutor.Iface
{
	private Connection myConnection;

	@Override
	public void connect(String url, Map<String, String> params) throws FailError, TException
	{
		Properties properties = new Properties();
		properties.putAll(params);

		try
		{
			myConnection = DriverManager.getConnection(url, properties);
		}
		catch(Throwable e)
		{
			e.printStackTrace();

			throw new FailError(e.getMessage(), getStackTrace(e));
		}
	}

	@Override
	public boolean testConnection() throws FailError, TException
	{
		return call(param ->
		{
			param.isValid(10_000);
			return true;
		});
	}

	@Override
	public List<String> listDatabases() throws FailError, TException
	{
		return call(conn ->
		{
			List<String> databases = new ArrayList<>();

			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet tables = metaData.getCatalogs();
			while(tables.next())
			{
				databases.add(tables.getString("TABLE_CAT"));
			}
			return databases;
		});
	}

	@Override
	public List<JdbcTable> listTables(String databaseName) throws FailError, TException
	{
		return call(conn ->
		{
			conn.setCatalog(databaseName);

			List<JdbcTable> list = new ArrayList<>();

			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while(rs.next())
			{
				String tableName = rs.getString(3);

				List<JdbcColum> columList = new ArrayList<>();

				ResultSet columns = md.getColumns(null, null, tableName, null);
				while(columns.next())
				{
					String colName = columns.getString(4);
					int jdbcColType = columns.getInt(5);
					String colType = columns.getString(6);

					columList.add(new JdbcColum(colName, colType, jdbcColType));
				}
				list.add(new JdbcTable(tableName, columList));
			}

			return list;
		});
	}

	@Override
	public JdbcQueryResult runQuery(String query, List<JdbcValue> params) throws FailError, TException
	{
		return call(conn ->
		{
			PreparedStatement statement = conn.prepareStatement(query);
			int i = 1;
			for(JdbcValue param : params)
			{
				switch(param.getType())
				{
					case _int:
						statement.setInt(1, param.getIntValue());
						break;
					case _long:
						statement.setLong(1, param.getLongValue());
						break;
					case _string:
						statement.setString(i, param.getStringValue());
						break;
					case _bool:
						statement.setBoolean(i, param.isBoolValue());
						break;
					default:
						throw new UnsupportedOperationException(param.getType() + " not handled");
				}

				i++;
			}

			ResultSet resultSet = statement.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			List<String> columns = new ArrayList<>();
			int[] columnsTypes = new int[columnCount];
			for(int j = 0; j < columnCount; j++)
			{
				String columnClassName = metaData.getColumnName(j + 1);
				columns.add(columnClassName);
				columnsTypes[j] = metaData.getColumnType(j + 1);
			}

			JdbcQueryResult result = new JdbcQueryResult();
			result.setColumns(columns);

			List<JdbcQueryRow> rows = new ArrayList<>();
			result.setRows(rows);

			while(resultSet.next())
			{
				JdbcQueryRow row = new JdbcQueryRow();
				rows.add(row);
				List<JdbcValue> values = new ArrayList<>();
				row.setValues(values);

				for(int j = 0; j < columnCount; j++)
				{
					int columnType = columnsTypes[j];

					JdbcValue value = new JdbcValue();
					values.add(value);
					switch(columnType)
					{
						case Types.BIGINT:
							value.setType(JdbcValueType._long);
							value.setLongValue(resultSet.getLong(j + 1));
							break;
						case Types.INTEGER:
						case Types.SMALLINT:
						case Types.TINYINT:
							value.setType(JdbcValueType._int);
							value.setIntValue(resultSet.getInt(j + 1));
							break;
						case Types.BOOLEAN:
							value.setType(JdbcValueType._bool);
							value.setBoolValue(resultSet.getBoolean(j + 1));
							break;
						case Types.VARCHAR:
							value.setType(JdbcValueType._string);
							value.setStringValue(resultSet.getString(j + 1));
							break;
						default:
							// FIXME [VISTALL] fallback
							value.setType(JdbcValueType._string);
							value.setStringValue(resultSet.getString(j + 1));
							break;
					}
				}
			}
			return result;
		});
	}

	@Override
	public void setDatabase(String dbName) throws FailError, TException
	{
		call(param ->
		{
			param.setCatalog(dbName);
			return null;
		});
	}

	private String getStackTrace(Throwable t)
	{
		StringWriter writer = new StringWriter();
		try (PrintWriter printWriter = new PrintWriter(writer))
		{
			t.printStackTrace(printWriter);
		}
		return writer.getBuffer().toString();
	}

	protected <T> T call(ThwroableFunction<T, Connection> callable) throws FailError
	{
		try
		{
			if(myConnection == null)
			{
				throw new SQLException("Not connected");
			}

			return callable.call(myConnection);
		}
		catch(Throwable e)
		{
			e.printStackTrace();

			throw new FailError(e.getMessage(), getStackTrace(e));
		}
	}
}
