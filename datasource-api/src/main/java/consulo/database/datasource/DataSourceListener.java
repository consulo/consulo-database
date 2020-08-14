package consulo.database.datasource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public interface DataSourceListener
{
	default void dataSourceChanged(@Nonnull DataSource dataSource)
	{
	}
}
