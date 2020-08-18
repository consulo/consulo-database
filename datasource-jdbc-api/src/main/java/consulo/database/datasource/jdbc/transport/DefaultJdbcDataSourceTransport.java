package consulo.database.datasource.jdbc.transport;

import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.PathUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.DownloadUtil;
import consulo.container.boot.ContainerPathManager;
import consulo.container.plugin.PluginManager;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.util.dataholder.Key;
import consulo.util.lang.ref.SimpleReference;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TSocket;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class DefaultJdbcDataSourceTransport implements DataSourceTransport
{
	private static final Logger LOG = Logger.getInstance(DefaultJdbcDataSourceTransport.class);

	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return dataSource.getProvider() instanceof JdbcDataSourceProvider;
	}

	@Override
	public void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result)
	{
		JdbcDataSourceProvider provider = (JdbcDataSourceProvider) dataSource.getProvider();

		LinkedHashMap<String, String> drivers = new LinkedHashMap<>();

		provider.fillDrivers(drivers);

		Map.Entry<String, String> selectedDriver = ContainerUtil.getFirstItem(drivers.entrySet());

		Path driverPath = Paths.get(ContainerPathManager.get().getSystemPath(), "datasource-drivers", provider.getId(), selectedDriver.getKey());

		if(!Files.exists(driverPath))
		{
			String url = selectedDriver.getValue();

			try
			{
				Files.createDirectories(driverPath.getParent());

				DownloadUtil.downloadContentToFile(indicator, url, driverPath.toFile());
			}
			catch(IOException e)
			{
				result.rejectWithThrowable(e);
				return;
			}
		}

		String jdbcUrl = provider.buildJdbcUrl(dataSource);

		String login = dataSource.getProperties().get(GenericPropertyKeys.LOGIN);
		String password = dataSource.getProperties().get(GenericPropertyKeys.PASSWORD);

		Map<String, String> properties = new HashMap<>();
		properties.put("user", login);
		properties.put("password", password);

		SimpleReference<OSProcessHandler> processRef = SimpleReference.create();

		SimpleReference<Integer> exitCodeRef = SimpleReference.create();

		SimpleReference<Future<?>> watcherRef = SimpleReference.create(CompletableFuture.completedFuture(null));

		try
		{
			Platform platform = Platform.current();

			String java_home = platform.os().getEnvironmentVariable("JAVA_HOME");

			SimpleJavaParameters simpleJavaParameters = new SimpleJavaParameters();
			simpleJavaParameters.getClassPath().add(new File(PluginManager.getPluginPath(DefaultJdbcDataSourceTransport.class), "rt/consulo.database-datasource.jdbc.rt.jar"));
			simpleJavaParameters.getClassPath().add(PathUtil.getJarPathForClass(JdbcExecutor.class));
			simpleJavaParameters.getClassPath().add(PathUtil.getJarPathForClass(TServer.class));
			simpleJavaParameters.getClassPath().add(PathUtil.getJarPathForClass(org.slf4j.Logger.class));
			simpleJavaParameters.getClassPath().add(driverPath.toFile());
			simpleJavaParameters.setMainClass("consulo.database.jdbc.rt.Main");
			simpleJavaParameters.setJdk(new FakeSdk(java_home));

			OSProcessHandler processHandler = simpleJavaParameters.createOSProcessHandler();
			processRef.set(processHandler);

			processHandler.addProcessListener(new ProcessAdapter()
			{
				@Override
				public void onTextAvailable(ProcessEvent event, Key outputType)
				{
					if(event.getText().trim().equals("[binded]"))
					{
						scheduleConnectAsync(result, jdbcUrl, properties, processRef);
					}
				}

				@Override
				public void processTerminated(ProcessEvent event)
				{
					exitCodeRef.setIfNull(event.getExitCode());


					if(event.getExitCode() != 0)
					{
						result.rejectWithThrowable(new ProcessCanceledException());
					}
					else
					{
						result.setDone(null);
					}
				}
			});

			ScheduledFuture<?> future = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() ->
			{
				// process terminated
				if(exitCodeRef.get() != null)
				{
					watcherRef.get().cancel(false);
					return;
				}

				if(indicator.isCanceled())
				{
					OSProcessHandler handler = processRef.get();
					if(handler != null)
					{
						// reset ref - no need kill process twice
						processRef.set(null);
						handler.destroyProcess();
					}
					return;
				}

			}, 1, 1, TimeUnit.SECONDS);

			watcherRef.set(future);

			processHandler.startNotify();
		}
		catch(Throwable e)
		{
			LOG.warn(e);

			result.rejectWithThrowable(e);
		}
	}

	protected void scheduleConnectAsync(AsyncResult<Void> result, String jdbcUrl, Map<String, String> properties, SimpleReference<OSProcessHandler> processHandler)
	{
		AppExecutorUtil.getAppExecutorService().execute(() -> scheduleConnectImpl(result, jdbcUrl, properties, processHandler));
	}

	protected void scheduleConnectImpl(AsyncResult<Void> result, String jdbcUrl, Map<String, String> properties, SimpleReference<OSProcessHandler> processHandler)
	{
		try
		{
			TSocket socket = new TSocket("localhost", 6645);
			socket.open();

			JdbcExecutor.Client client = new JdbcExecutor.Client(new TBinaryProtocol(socket));

			boolean testResult = client.testConnection(jdbcUrl, properties);
			if(testResult)
			{
				result.setDone();
			}
			else
			{
				result.rejectWithThrowable(new Exception("Failed"));
			}

			socket.close();
		}
		catch(Throwable e)
		{
			LOG.warn(e);

			result.rejectWithThrowable(e);
		}

		OSProcessHandler handler = processHandler.get();
		if(handler != null)
		{
			handler.destroyProcess();
		}
	}
}
