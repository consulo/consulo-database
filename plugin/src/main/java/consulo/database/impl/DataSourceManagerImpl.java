package consulo.database.impl;

import com.intellij.openapi.project.Project;
import consulo.database.datasource.DataSource;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
@Singleton
public class DataSourceManagerImpl implements DataSourceManager
{
	private final Project myProject;

	private List<DataSource> myDataSources = new ArrayList<>();

	@Inject
	public DataSourceManagerImpl(Project project)
	{
		myProject = project;

		for(DataSourceProvider dataSourceProvider : DataSourceProvider.EP_NAME.getExtensionList())
		{
			myDataSources.add(new DataSourceImpl(dataSourceProvider.getName().get() + " Test", dataSourceProvider, this));
		}
	}

	@Nonnull
	@Override
	public List<DataSource> getDataSources()
	{
		return myDataSources;
	}

	public void notifyChanged(@Nonnull DataSource dataSource)
	{
		myProject.getMessageBus().syncPublisher(TOPIC).dataSourceChanged(dataSource);
	}
}
