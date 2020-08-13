package consulo.database.impl;

import com.intellij.openapi.project.Project;
import consulo.database.datasource.DataSource;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
@Singleton
public class DataSourceManagerImpl implements DataSourceManager
{
	private final Project myProject;

	@Inject
	public DataSourceManagerImpl(Project project)
	{
		myProject = project;
	}

	@Nonnull
	@Override
	public List<DataSource> getDataSources()
	{
		DataSourceProvider provider = DataSourceProvider.EP_NAME.getExtensionList().get(0);

		return Collections.singletonList(new DataSourceImpl("Test", provider));
	}
}
