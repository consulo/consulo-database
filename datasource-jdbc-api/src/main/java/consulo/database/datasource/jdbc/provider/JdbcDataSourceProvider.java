package consulo.database.datasource.jdbc.provider;

import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.concurrency.AppExecutorUtil;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public abstract class JdbcDataSourceProvider implements DataSourceProvider
{
	@Nonnull
	public String buildJdbcUrl(@Nonnull DataSource dataSource)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("jdbc:");
		builder.append(getId());
		builder.append("://");

		String host = dataSource.getProperties().get(GenericPropertyKeys.HOST);
		builder.append(host);

		int port = dataSource.getProperties().get(GenericPropertyKeys.PORT);
		builder.append(":");
		builder.append(port);

		String databaseName = dataSource.getProperties().get(GenericPropertyKeys.DATABASE_NAME);
		builder.append("/");
		builder.append(databaseName);

		return builder.toString();
	}

	@Nonnull
	@Override
	public AsyncResult<Void> testConnection(@Nonnull DataSource dataSource)
	{
		String jdbcUrl = buildJdbcUrl(dataSource);

		String login = dataSource.getProperties().get(GenericPropertyKeys.LOGIN);
		String password = dataSource.getProperties().get(GenericPropertyKeys.PASSWORD);

		AsyncResult<Void> result = AsyncResult.undefined();

		AppExecutorUtil.getAppExecutorService().execute(() ->
		{
			try
			{

				Class<Driver> aClass = (Class<Driver>) Class.forName("com.mysql.cj.jdbc.Driver", true, JdbcDataSourceProvider.class.getClassLoader());

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

		return result;
	}
}
