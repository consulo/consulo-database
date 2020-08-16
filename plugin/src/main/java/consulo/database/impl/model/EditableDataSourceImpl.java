package consulo.database.impl.model;

import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.configurable.EditablePropertiesHolderImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public class EditableDataSourceImpl extends DataSourceImpl implements EditableDataSource
{
	private DataSourceImpl myOriginal;

	public EditableDataSourceImpl(DataSourceImpl original)
	{
		super(original.myModel);

		myOriginal = original;

		myId = original.getId();
		myName = original.getName();
		myProvider = original.getProvider();

		myPropertiesHolder = new EditablePropertiesHolderImpl();
		myPropertiesHolder.replaceAll(original.myPropertiesHolder);
	}

	public EditableDataSourceImpl(DataSourceModel manager, String name, DataSourceProvider provider)
	{
		super(manager);

		myName = name;
		myProvider = provider;
		myPropertiesHolder = new EditablePropertiesHolderImpl();
	}

	@Nonnull
	@Override
	public EditablePropertiesHolder getProperties()
	{
		return (EditablePropertiesHolder) super.getProperties();
	}

	@Override
	public void setName(@Nonnull String name)
	{
		myName = name;
	}

	@Nullable
	public DataSourceImpl getOriginal()
	{
		return myOriginal;
	}
}
