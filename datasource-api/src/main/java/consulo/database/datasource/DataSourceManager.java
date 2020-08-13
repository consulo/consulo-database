package consulo.database.datasource;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public interface DataSourceManager
{
	@Nonnull
	public static DataSourceManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, DataSourceManager.class);
	}

	@Nonnull
	List<DataSource> getDataSources();
}
