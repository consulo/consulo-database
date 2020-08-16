package consulo.database.datasource.transport;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public interface DataSourceTransportManager
{
	@Nonnull
	static DataSourceTransportManager getInstance()
	{
		return ServiceManager.getService(DataSourceTransportManager.class);
	}

	@Nonnull
	AsyncResult<Void> testConnection(@Nonnull Project project, @Nonnull DataSource dataSource);
}
