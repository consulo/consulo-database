package consulo.database.jdbc.rt;

import consulo.database.jdbc.rt.shared.JdbcExecutor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransport;

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

		TServerSocket transport = new TServerSocket(new InetSocketAddress("localhost", 6645));

		TSimpleServer.Args serverArgs = new TSimpleServer.Args(transport);
		serverArgs = serverArgs.processor(p);

		TSimpleServer server = new TSimpleServer(serverArgs);
		server.setServerEventHandler(new TServerEventHandler()
		{
			@Override
			public void preServe()
			{
				System.out.println("[binded]");
			}

			@Override
			public ServerContext createContext(TProtocol tProtocol, TProtocol tProtocol1)
			{
				return null;
			}

			@Override
			public void deleteContext(ServerContext serverContext, TProtocol tProtocol, TProtocol tProtocol1)
			{
			}

			@Override
			public void processContext(ServerContext serverContext, TTransport tTransport, TTransport tTransport1)
			{
			}
		});

		server.serve();
	}
}
