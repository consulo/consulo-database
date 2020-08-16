package consulo.database.jdbc.rt;

import consulo.database.jdbc.rt.shared.JdbcExecutor;
import org.apache.thrift.TException;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class JdbcExecutorImpl implements JdbcExecutor.Iface
{
	@Override
	public boolean testConnection() throws TException
	{
		return false;
	}
}
