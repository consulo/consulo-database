package consulo.database.datasource.configurable;

import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface EditablePropertiesHolder extends PropertiesHolder
{
	<T> void set(@Nonnull Key<T> key, @Nullable T value);
}
