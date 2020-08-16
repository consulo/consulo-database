package consulo.database.impl.model;

import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceImpl implements DataSource
{
	protected String myId;
	protected String myName;
	protected DataSourceProvider myProvider;

	protected final DataSourceModel myModel;

	public DataSourceImpl(String id, String name, DataSourceProvider provider, DataSourceModel model)
	{
		this(model);
		myId = id;
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

	@Nonnull
	@Override
	public String getId()
	{
		if(myId == null)
		{
			myId = UUID.randomUUID().toString();
		}
		return myId;
	}
}
