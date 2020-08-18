package consulo.database.datasource.jdbc.transport;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.jdbc.provider.impl.JdbcState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import consulo.database.jdbc.rt.shared.JdbcTable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class DefaultJdbcDataSourceTransport implements DataSourceTransport<JdbcState>
{
	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return dataSource.getProvider() instanceof JdbcDataSourceProvider;
	}

	@Override
	public void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result)
	{
		JdbcSession<Boolean> session = new JdbcSession<>(JdbcExecutor.Client::testConnection);

		AsyncResult<Boolean> sessionResult = session.run(indicator, dataSource);
		sessionResult.doWhenRejectedWithThrowable(result::rejectWithThrowable);

		sessionResult.doWhenDone(valid -> {
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
		JdbcSession<List<JdbcTable>> session = new JdbcSession<>(JdbcExecutor.Client::listTables);

		AsyncResult<List<JdbcTable>> sessionResult = session.run(indicator, dataSource);

		sessionResult.doWhenRejectedWithThrowable(result::rejectWithThrowable);

		sessionResult.doWhenDone(jdbcTables ->
		{
			JdbcState state = new JdbcState();

			for(JdbcTable jdbcTable : jdbcTables)
			{
				JdbcTableState tableState = new JdbcTableState();
				tableState.setName(jdbcTable.getName());

				state.addTable(tableState);
			}

			result.setDone(state);
		});
	}

	@Nonnull
	@Override
	public Class<JdbcState> getStateClass()
	{
		return JdbcState.class;
	}
}