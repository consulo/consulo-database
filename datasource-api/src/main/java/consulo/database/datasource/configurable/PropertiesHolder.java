package consulo.database.datasource.configurable;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface PropertiesHolder
{
	<T> T get(@Nonnull GenericPropertyKey<T> key);
}
