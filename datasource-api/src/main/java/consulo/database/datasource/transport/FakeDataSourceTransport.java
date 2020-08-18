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
public class FakeDataSourceTransport implements DataSourceTransport
{
	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return true;
	}

	@Override
	public void testConnection(ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result)
	{
		result.setDone();
	}
}
