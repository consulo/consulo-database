package consulo.database.datasource.model;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public interface DataSourceModel
{
	@Nonnull
	List<? extends DataSource> getDataSources();
}
