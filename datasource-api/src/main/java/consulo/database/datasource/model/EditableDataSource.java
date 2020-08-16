package consulo.database.datasource.model;

import consulo.database.datasource.configurable.EditablePropertiesHolder;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public interface EditableDataSource extends DataSource
{
	void setName(@Nonnull String name);

	@Override
	@Nonnull
	EditablePropertiesHolder getProperties();
}
