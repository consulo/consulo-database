package consulo.database.datasource.jdbc.transport;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ThrowableConsumer;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.jdbc.provider.impl.*;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.jdbc.rt.shared.JdbcColum;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import consulo.database.jdbc.rt.shared.JdbcTable;
import consulo.logging.Logger;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class DefaultJdbcDataSourceTransport implements DataSourceTransport<JdbcState>
{
	private static final Logger LOG = Logger.getInstance(DefaultJdbcDataSourceTransport.class);

	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return dataSource.getProvider() instanceof JdbcDataSourceProvider;
	}

	private <T> void safeCall(@Nonnull ProgressIndicator indicator, @Nonnull DataSource dataSource, @Nonnull AsyncResult<T> result, @Nonnull ThrowableConsumer<JdbcSession, Throwable> sessionConsumer)
	{
		try (JdbcSession session = new JdbcSession(indicator, dataSource))
		{
			sessionConsumer.consume(session);
		}
		catch(ProcessCanceledException e)
		{
			result.rejectWithThrowable(e);
		}
		catch(Throwable e)
		{
			LOG.warn(e);

			result.rejectWithThrowable(e);
		}
	}

	@Override
	public void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result)
	{
		safeCall(indicator, dataSource, result, session ->
		{
			Boolean valid = session.execute(JdbcExecutor.Client::testConnection);

			if(valid)
			{
				result.setDone();
			}
			else
			{
				result.rejectWithThrowable(new Error("Failed"));
			}
		});
	}

	@Override
	public void loadInitialData(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<JdbcState> result)
	{
		String databaseName = StringUtil.nullize(dataSource.getProperties().get(GenericPropertyKeys.DATABASE_NAME));

		safeCall(indicator, dataSource, result, session ->
		{
			JdbcState state = new JdbcState();

			if(databaseName == null)
			{
				List<String> databases = session.execute(JdbcExecutor.Client::listDatabases);

				for(String database : databases)
				{
					indicator.setText("Processsing '" + dataSource.getName() + ":" + database + "'");

					List<JdbcTable> jdbcTables = session.execute(client -> client.listTables(database));

					state.addDatabase(database, new JdbcDatabaseState(database, convertToTables(jdbcTables)));
				}
			}
			else
			{
				indicator.setText("Processsing '" + dataSource.getName() + ":" + databaseName + "'");

				List<JdbcTable> jdbcTables = session.execute(client -> client.listTables(databaseName));

				state.addDatabase(databaseName, new JdbcDatabaseState(databaseName, convertToTables(jdbcTables)));
			}

			result.setDone(state);
		});
	}

	@Nonnull
	private static JdbcTablesState convertToTables(List<JdbcTable> jdbcTables)
	{
		JdbcTablesState state = new JdbcTablesState();
		for(JdbcTable jdbcTable : jdbcTables)
		{
			JdbcTableState tableState = new JdbcTableState();
			tableState.setName(jdbcTable.getName());

			for(JdbcColum colum : jdbcTable.getColums())
			{
				tableState.addColumn(new JdbcTableColumState(colum.getName(), colum.getType()));
			}
			state.addTable(tableState);
		}
		return state;
	}

	@Nonnull
	@Override
	public Class<JdbcState> getStateClass()
	{
		return JdbcState.class;
	}
}