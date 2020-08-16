package consulo.database.jdbc.rt;

import consulo.database.jdbc.rt.shared.JdbcExecutor;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;

import java.net.InetSocketAddress;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class Main
{
	public static void main(String[] args) throws Exception
	{
		JdbcExecutor.Processor<JdbcExecutorImpl> p = new JdbcExecutor.Processor<>(new JdbcExecutorImpl());

		TNonblockingServerSocket transport = new TNonblockingServerSocket(new InetSocketAddress("localhost", 6645));

		TThreadedSelectorServer.Args serverArgs = new TThreadedSelectorServer.Args(transport);

		serverArgs = serverArgs.processor(p);

		TThreadedSelectorServer server = new TThreadedSelectorServer(serverArgs);

		server.serve();
	}
}
