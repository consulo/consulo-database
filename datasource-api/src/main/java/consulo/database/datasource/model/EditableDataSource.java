package consulo.database.datasource.model;

import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public interface EditableDataSource extends DataSource
{
	void setName(@Nonnull String name);
}
