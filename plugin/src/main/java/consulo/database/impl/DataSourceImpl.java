package consulo.database.impl;

import consulo.database.datasource.DataSource;
import consulo.database.datasource.DataSourceModel;
import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceImpl implements DataSource
{
	protected String myName;
	protected DataSourceProvider myProvider;

	protected final DataSourceModel myModel;

	public DataSourceImpl(String name, DataSourceProvider provider, DataSourceModel model)
	{
		this(model);
		myName = name;
		myProvider = provider;
	}

	protected DataSourceImpl(DataSourceModel model)
	{
		myModel = model;
	}

	public void copyFrom(DataSourceImpl dataSource)
	{
		myName = dataSource.getName();
		myProvider = dataSource.getProvider();
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
