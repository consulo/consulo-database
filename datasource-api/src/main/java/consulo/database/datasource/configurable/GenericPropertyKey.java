package consulo.database.datasource.configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public final class GenericPropertyKey<T>
{
	@Nonnull
	public static <K> GenericPropertyKey<K> create(@Nonnull String name, @Nonnull Class<K> aTypeClass)
	{
		return create(name, aTypeClass, null);
	}

	@Nonnull
	public static <K> GenericPropertyKey<K> create(@Nonnull String name, @Nonnull Class<K> aTypeClass, @Nullable K defautValue)
	{
		return new GenericPropertyKey<>(name, aTypeClass, defautValue);
	}

	private final String myName;
	private final Class<T> myTypeClass;
	private final T myDefautValue;

	private GenericPropertyKey(@Nonnull String name, @Nonnull Class<T> aTypeClass, @Nullable T defautValue)
	{
		myName = name;
		myTypeClass = aTypeClass;
		myDefautValue = defautValue;
	}

	public String getName()
	{
		return myName;
	}

	public Class<T> getTypeClass()
	{
		return myTypeClass;
	}

	@Nullable
	public T getDefautValue()
	{
		return myDefautValue;
	}

	@Override
	public String toString()
	{
		return myName;
	}
}
