package consulo.database.impl.configurable;

import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.util.dataholder.Key;
import consulo.util.dataholder.KeyWithDefaultValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class PropertiesHolderImpl implements PropertiesHolder
{
	protected Map<String, Object> myValues = new HashMap<>();

	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> T get(@Nonnull Key<T> key)
	{
		T value = (T) myValues.get(key.toString());
		if(value == null && key instanceof KeyWithDefaultValue)
		{
			return ((KeyWithDefaultValue<T>) key).getDefaultValue();
		}
		return value;
	}

	public void replaceAll(PropertiesHolderImpl other)
	{
		myValues.clear();
		myValues.putAll(other.myValues);
	}
}
