package consulo.database.datasource.model;

import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public interface EditableDataSourceModel extends DataSourceModel
{
	void addListener(DataSourceListener listener);

	@Nonnull
	@Override
	List<? extends EditableDataSource> getDataSources();

	EditableDataSource newDataSource(@Nonnull String name, @Nonnull DataSourceProvider dataSourceProvider);

	EditableDataSource newDataSourceCopy(@Nonnull String name, @Nonnull DataSource dataSource);

	default void removeDataSource(@Nonnull String name)
	{
		DataSource dataSource = findDataSource(name);
		if(dataSource == null)
		{
			throw new IllegalArgumentException("Didn't found datasource with name: " + name);
		}

		removeDataSource(dataSource);
	}

	void removeDataSource(@Nonnull DataSource dataSource);

	void commit();

	void dispose();
}
