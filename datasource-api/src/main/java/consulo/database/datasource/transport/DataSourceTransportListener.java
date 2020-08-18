package consulo.database.datasource.transport;

import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public interface DataSourceTransportListener
{
	void dataUpdated(@Nonnull DataSource dataSource, @Nonnull Object value);
}
