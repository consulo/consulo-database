package consulo.database.datasource.configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface EditablePropertiesHolder extends PropertiesHolder
{
	<T> void set(@Nonnull GenericPropertyKey<T> key, @Nullable T value);
}
