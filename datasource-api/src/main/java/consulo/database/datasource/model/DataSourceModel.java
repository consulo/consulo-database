package consulo.database.datasource.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public interface DataSourceModel
{
	@Nonnull
	List<? extends DataSource> getDataSources();

	@Nullable
	default DataSource findDataSource(@Nonnull UUID id)
	{
		for(DataSource source : getDataSources())
		{
			if(Objects.equals(source.getId(), id))
			{
				return source;
			}
		}

		return null;
	}
}
