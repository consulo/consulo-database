package consulo.database.datasource.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public interface DataSourceModel
{
	@Nonnull
	List<? extends DataSource> getDataSources();

	@Nullable
	default DataSource findDataSource(@Nonnull String name)
	{
		for(DataSource source : getDataSources())
		{
			if(Objects.equals(source.getName(), name))
			{
				return source;
			}
		}

		return null;
	}
}
