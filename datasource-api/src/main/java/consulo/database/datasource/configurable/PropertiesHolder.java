package consulo.database.datasource.configurable;

import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface PropertiesHolder
{
	<T> T get(@Nonnull Key<T> key);
}
