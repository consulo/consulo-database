package consulo.database.datasource;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceListener;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.model.EditableDataSourceModel;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public interface DataSourceManager
{
	Topic<DataSourceListener> TOPIC = Topic.create("datasource change listener", DataSourceListener.class);

	@Nonnull
	public static DataSourceManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, DataSourceManager.class);
	}

	@Nonnull
	@RequiredReadAction
	default List<? extends DataSource> getDataSources()
	{
		 return getModel().getDataSources();
	}

	/**
	 * @return readonly datasource model
	 */
	@Nonnull
	@RequiredReadAction
	DataSourceModel getModel();

	@Nonnull
	EditableDataSourceModel createEditableModel();
}
