package consulo.database.datasource.transport;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.messages.Topic;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public interface DataSourceTransportManager
{
	Topic<DataSourceTransportListener> TOPIC = Topic.create("DataSourceTransportListener", DataSourceTransportListener.class);

	@Nonnull
	static DataSourceTransportManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, DataSourceTransportManager.class);
	}

	@Nonnull
	AsyncResult<Void> testConnection(@Nonnull DataSource dataSource);

	@RequiredReadAction
	void refreshAll();

	<T extends PersistentStateComponent<?>> T getDataState(@Nonnull DataSource dataSource);
}
