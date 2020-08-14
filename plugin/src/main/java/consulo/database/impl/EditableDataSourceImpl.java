package consulo.database.impl;

import consulo.database.datasource.EditableDataSource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public class EditableDataSourceImpl extends DataSourceImpl implements EditableDataSource
{
	private final DataSourceImpl myOriginal;

	public EditableDataSourceImpl(DataSourceImpl original)
	{
		super(original.myDataSourceManager);

		myOriginal = original;

		myName = original.getName();
		myProvider = original.getProvider();
	}

	@Override
	public void setName(@Nonnull String name)
	{
		myName = name;
	}

	@Override
	public void commit()
	{
		myOriginal.myName = myName;

		myDataSourceManager.notifyChanged(myOriginal);
	}

	@Nonnull
	@Override
	public EditableDataSource wantEdit()
	{
		throw new UnsupportedOperationException("already editable");
	}
}
