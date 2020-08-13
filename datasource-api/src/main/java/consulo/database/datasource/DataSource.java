package consulo.database.datasource;

import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public interface DataSource
{
	@Nonnull
	DataSourceProvider getProvider();

	@Nonnull
	String getName();
}
