package consulo.database.datasource.jdbc.transport;

import consulo.database.jdbc.rt.shared.JdbcExecutor;
import org.apache.thrift.TException;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public interface JdbcExecutorAction<T>
{
	T run(JdbcExecutor.Client client) throws TException;
}
