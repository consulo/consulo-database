package consulo.database.datasource.jdbc.transport;

import consulo.database.jdbc.rt.shared.JdbcExecutor;
import org.apache.thrift.TException;

import java.util.Map;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public interface JdbcExecutorAction<T>
{
	T run(JdbcExecutor.Client client, String url, Map<String, String> params) throws TException;
}
