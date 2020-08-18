package consulo.database.datasource.transport;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class FakeDataSourceTransport implements DataSourceTransport<FakeResult>
{
	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return true;
	}

	@Override
	public void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result)
	{
		result.setDone();
	}

	@Override
	public void loadInitialData(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<FakeResult> result)
	{
		result.rejectWithThrowable(new UnsupportedOperationException());
	}

	@Nonnull
	@Override
	public Class<FakeResult> getStateClass()
	{
		return FakeResult.class;
	}
}
