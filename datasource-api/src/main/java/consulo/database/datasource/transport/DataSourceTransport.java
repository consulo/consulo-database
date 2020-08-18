package consulo.database.datasource.transport;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface DataSourceTransport
{
	ExtensionPointName<DataSourceTransport> EP_NAME = ExtensionPointName.create("consulo.database.transport");

	boolean accept(@Nonnull DataSource dataSource);

	void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result);
}
