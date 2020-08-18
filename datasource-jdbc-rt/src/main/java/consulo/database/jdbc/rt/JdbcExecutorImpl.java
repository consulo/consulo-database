package consulo.database.jdbc.rt;

import consulo.database.jdbc.rt.shared.FailError;
import consulo.database.jdbc.rt.shared.JdbcColum;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import consulo.database.jdbc.rt.shared.JdbcTable;
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
					String colType = columns.getString(6);

					columList.add(new JdbcColum(colName, colType));
				}
				list.add(new JdbcTable(tableName, columList));
			}

			return list;
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
