package consulo.database.jdbc.rt;

import consulo.database.jdbc.rt.shared.FailError;
import consulo.database.jdbc.rt.shared.JdbcColum;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import consulo.database.jdbc.rt.shared.JdbcTable;
import org.apache.thrift.TException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
	@Override
	public boolean testConnection(String url, Map<String, String> params) throws TException
	{
		return call(connection -> connection.isValid(5000), url, params);
	}

	@Override
	public List<JdbcTable> listTables(String url, Map<String, String> params) throws FailError, TException
	{
		return call(conn ->
		{
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
		}, url, params);
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

	protected <T> T call(ThwroableFunction<T, Connection> callable, String url, Map<String, String> params) throws FailError
	{
		Properties properties = new Properties();
		properties.putAll(params);

		try
		{
			Connection connection = DriverManager.getConnection(url, properties);

			return callable.call(connection);
		}
		catch(Throwable e)
		{
			e.printStackTrace();

			throw new FailError(e.getMessage(), getStackTrace(e));
		}
	}
}
