package consulo.database.datasource.model;

import consulo.database.datasource.configurable.PropertiesHolder;
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

	/**
	 * @return unique uuid for datasource. this id never change
	 */
	@Nonnull
	String getId();

	@Nonnull
	PropertiesHolder getProperties();
}
