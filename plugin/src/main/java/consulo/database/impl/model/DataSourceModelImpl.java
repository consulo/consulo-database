package consulo.database.impl.model;

import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceModel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class DataSourceModelImpl<T extends DataSource> implements DataSourceModel
{
	protected List<T> myDataSources = new ArrayList<>();

	@Nonnull
	@Override
	public List<? extends DataSource> getDataSources()
	{
		return Collections.unmodifiableList(myDataSources);
	}

	public void replaceAll(@Nonnull List<T> newSources)
	{
		myDataSources.clear();
		myDataSources.addAll(newSources);
	}
}
