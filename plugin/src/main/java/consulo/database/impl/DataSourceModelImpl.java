package consulo.database.impl;

import consulo.database.datasource.DataSource;
import consulo.database.datasource.DataSourceModel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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
		return myDataSources;
	}
}
