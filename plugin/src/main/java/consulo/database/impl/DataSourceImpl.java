package consulo.database.impl;

import consulo.database.datasource.DataSource;
import consulo.database.datasource.EditableDataSource;
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

	protected final DataSourceManagerImpl myDataSourceManager;

	public DataSourceImpl(String name, DataSourceProvider provider, DataSourceManagerImpl dataSourceManager)
	{
		this(dataSourceManager);
		myName = name;
		myProvider = provider;
	}

	protected DataSourceImpl(DataSourceManagerImpl dataSourceManager)
	{
		myDataSourceManager = dataSourceManager;
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

	@Nonnull
	@Override
	public EditableDataSource wantEdit()
	{
		return new EditableDataSourceImpl(this);
	}
}
