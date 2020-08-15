package consulo.database.datasource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public interface EditableDataSource extends DataSource
{
	void setName(@Nonnull String name);
}
