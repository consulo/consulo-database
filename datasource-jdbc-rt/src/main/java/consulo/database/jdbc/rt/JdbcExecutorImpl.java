package consulo.database.jdbc.rt;

import consulo.database.jdbc.rt.shared.FailError;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import org.apache.thrift.TException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class JdbcExecutorImpl implements JdbcExecutor.Iface
{
	@Override
	public boolean testConnection(String url, Map<String, String> from) throws TException
	{
		Properties properties = new Properties();
		properties.putAll(from);

		try
		{
			Connection connection = DriverManager.getConnection(url, properties);

			connection.isValid(5000);

			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();

			throw new FailError(e.getMessage(), getStackTrace(e));
		}
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
}
