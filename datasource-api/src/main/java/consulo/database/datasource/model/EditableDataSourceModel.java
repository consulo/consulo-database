package consulo.database.datasource.model;

import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

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

	default void removeDataSource(@Nonnull String name)
	{
		for(EditableDataSource source : getDataSources())
		{
			if(Objects.equals(source.getName(), name))
			{
				removeDataSource(source);
				return;
			}
		}

		throw new UnsupportedOperationException("Didn't found datasource with name: " + name);
	}

	void removeDataSource(@Nonnull DataSource dataSource);

	void commit();

	void dispose();
}
