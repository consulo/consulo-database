package consulo.database.impl.model;

import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.provider.DataSourceProvider;

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
	}

	public EditableDataSourceImpl(DataSourceModel manager, String name, DataSourceProvider provider)
	{
		super(manager);

		myName = name;
		myProvider = provider;
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
