package consulo.database.impl;

import consulo.database.datasource.DataSource;
import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceImpl implements DataSource
{
	private final String myName;
	private final DataSourceProvider myProvider;

	public DataSourceImpl(String name, DataSourceProvider provider)
	{
		myName = name;
		myProvider = provider;
	}

	@Nonnull
	@Override
	public DataSourceProvider getProvider()
	{
		return myProvider;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return myName;
	}
}
