package consulo.database.datasource.jdbc.provider;

import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;

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
}
