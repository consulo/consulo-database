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
		int port = Integer.parseInt(args[0]);

		JdbcExecutor.Processor<JdbcExecutorImpl> p = new JdbcExecutor.Processor<>(new JdbcExecutorImpl());

		TServerSocket transport = new TServerSocket(new InetSocketAddress("localhost", port));

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
