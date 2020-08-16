package consulo.database.impl.configurable;

import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class EditablePropertiesHolderImpl extends PropertiesHolderImpl implements EditablePropertiesHolder
{
	@Override
	public <T> void set(@Nonnull Key<T> key, @Nullable T value)
	{
		if(value == null)
		{
			myValues.remove(key.toString());
		}
		else
		{
			myValues.put(key.toString(), value);
		}
	}
}
