package consulo.database.impl.model;

import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.configurable.PropertiesHolderImpl;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceImpl implements DataSource
{
	public static final String CONTAINER_NAME = "main";

	protected String myId;
	protected String myName;
	protected DataSourceProvider myProvider;

	protected final DataSourceModel myModel;

	protected PropertiesHolderImpl myPropertiesHolder;

	public DataSourceImpl(String id, String name, DataSourceProvider provider, DataSourceModel model)
	{
		this(model);
		myId = id;
		myName = name;
		myProvider = provider;
		myPropertiesHolder = new PropertiesHolderImpl(CONTAINER_NAME);
	}

	protected DataSourceImpl(DataSourceModel model)
	{
		myModel = model;
	}

	public void copyFrom(EditableDataSourceImpl dataSource)
	{
		myName = dataSource.getName();
		myProvider = dataSource.getProvider();
		myPropertiesHolder.copyFrom(dataSource.myPropertiesHolder);
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

	@Nonnull
	@Override
	public PropertiesHolder getProperties()
	{
		return myPropertiesHolder;
	}
}
