package consulo.database.impl.transport;

import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.datasource.transport.DataSourceTransportManager;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
@Singleton
public class DataSourceTransportManagerImpl implements DataSourceTransportManager
{
	@Nonnull
	@Override
	public AsyncResult<Void> testConnection(@Nonnull Project project, @Nonnull DataSource dataSource)
	{
		DataSourceTransport dataSourceTransport = null;
		for(DataSourceTransport transport : DataSourceTransport.EP_NAME.getExtensionList())
		{
			if(transport.accept(dataSource))
			{
				dataSourceTransport = transport;
				break;
			}
		}

		if(dataSourceTransport == null)
		{
			throw new UnsupportedOperationException("No fake transport. Broken distribution");
		}

		AsyncResult<Void> result = AsyncResult.undefined();

		final DataSourceTransport finalDataSourceTransport = dataSourceTransport;
		new Task.ConditionalModal(project, "Testing connection", true, PerformInBackgroundOption.DEAF)
		{
			@Override
			public void run(@Nonnull ProgressIndicator indicator)
			{
				finalDataSourceTransport.testConnection(indicator, project, dataSource, result);

				result.waitFor(-1);
			}
		}.queue();
		return result;
	}
}
