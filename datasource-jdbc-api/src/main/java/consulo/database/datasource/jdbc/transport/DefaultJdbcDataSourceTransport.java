package consulo.database.datasource.jdbc.transport;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.concurrency.AppExecutorUtil;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class DefaultJdbcDataSourceTransport implements DataSourceTransport
{
	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return dataSource.getProvider() instanceof JdbcDataSourceProvider;
	}

	@Override
	public void testConnection(@Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result)
	{
		JdbcDataSourceProvider provider = (JdbcDataSourceProvider) dataSource.getProvider();

		String jdbcUrl = provider.buildJdbcUrl(dataSource);

		String login = dataSource.getProperties().get(GenericPropertyKeys.LOGIN);
		String password = dataSource.getProperties().get(GenericPropertyKeys.PASSWORD);


		AppExecutorUtil.getAppExecutorService().execute(() ->
		{
			try
			{
				Class<Driver> aClass = (Class<Driver>) Class.forName("org.mariadb.jdbc.Driver", true, JdbcDataSourceProvider.class.getClassLoader());

				Driver driver = aClass.newInstance();

				Properties properties = new Properties();
				if(login != null)
				{
					properties.put("user", login);
				}

				if(password != null)
				{
					properties.put("password", password);
				}

				Connection connect = driver.connect(jdbcUrl, properties);

				connect.isValid(5000);

				result.setDone(null);
			}
			catch(Throwable e)
			{
				result.rejectWithThrowable(e);
			}
		});
	}
}
