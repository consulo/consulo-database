package consulo.database.impl.model;

import com.intellij.util.EventDispatcher;
import consulo.database.datasource.model.*;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.DataSourceManagerImpl;
import consulo.database.impl.configurable.PropertiesHolderImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class EditableDataSourceModelImpl extends DataSourceModelImpl<EditableDataSourceImpl> implements EditableDataSourceModel
{
	private final DataSourceManagerImpl myManager;

	@Nonnull
	private final DataSourceModelImpl<DataSourceImpl> myOriginalModel;

	private EventDispatcher<DataSourceListener> myDispatcher = EventDispatcher.create(DataSourceListener.class);

	private List<DataSource> myToRemoveDataSource = new ArrayList<>();

	public EditableDataSourceModelImpl(DataSourceManagerImpl manager, DataSourceModelImpl<DataSourceImpl> originalModel)
	{
		myManager = manager;
		myOriginalModel = originalModel;
		for(DataSource dataSource : originalModel.getDataSources())
		{
			myDataSources.add(new EditableDataSourceImpl((DataSourceImpl) dataSource));
		}
	}

	@Override
	public void addListener(DataSourceListener listener)
	{
		myDispatcher.addListener(listener);
	}

	@Nonnull
	@Override
	public List<? extends EditableDataSource> getDataSources()
	{
		return Collections.unmodifiableList(myDataSources);
	}

	@Override
	public EditableDataSource newDataSource(@Nonnull String name, @Nonnull DataSourceProvider dataSourceProvider)
	{
		EditableDataSourceImpl source = new EditableDataSourceImpl(this, name, dataSourceProvider);

		dataSourceProvider.fillDefaultProperties(source.getProperties());

		myDataSources.add(source);

		myDispatcher.getMulticaster().dataSourceEvent(new DataSourceEvent(myManager, DataSourceEvent.Action.ADD, source));

		return source;
	}

	@Override
	public EditableDataSource newDataSourceCopy(@Nonnull String name, @Nonnull DataSource original)
	{
		EditableDataSourceImpl newDataSource = new EditableDataSourceImpl(this, name, original.getProvider());

		newDataSource.myPropertiesHolder.copyFrom((PropertiesHolderImpl) original.getProperties());

		myDataSources.add(newDataSource);

		myDispatcher.getMulticaster().dataSourceEvent(new DataSourceEvent(myManager, DataSourceEvent.Action.ADD, newDataSource));

		return newDataSource;
	}

	@Override
	public void removeDataSource(@Nonnull DataSource dataSource)
	{
		if(!myDataSources.contains(dataSource))
		{
			throw new IllegalArgumentException("Not from this model");
		}

		myDataSources.remove(dataSource);

		EditableDataSourceImpl impl = (EditableDataSourceImpl) dataSource;

		DataSourceImpl original = impl.getOriginal();
		if(original != null)
		{
			myToRemoveDataSource.add(original);
		}

		myDispatcher.getMulticaster().dataSourceEvent(new DataSourceEvent(myManager, DataSourceEvent.Action.ADD, dataSource));
	}

	@Override
	public void commit()
	{
		List<DataSourceEvent> events = new ArrayList<>();

		for(EditableDataSourceImpl dataSource : myDataSources)
		{
			DataSourceImpl original = dataSource.getOriginal();

			if(original != null)
			{
				original.copyFrom(dataSource);

				events.add(new DataSourceEvent(myManager, DataSourceEvent.Action.CHANGE, original));
			}
			else
			{
				DataSourceImpl newDataSource = new DataSourceImpl(myOriginalModel);
				newDataSource.copyFrom(dataSource);

				myOriginalModel.myDataSources.add(newDataSource);

				events.add(new DataSourceEvent(myManager, DataSourceEvent.Action.CHANGE, newDataSource));
			}
		}

		for(DataSource dataSource : myToRemoveDataSource)
		{
			myOriginalModel.myDataSources.remove(dataSource);

			events.add(new DataSourceEvent(myManager, DataSourceEvent.Action.REMOVE, dataSource));
		}

		myManager.notifyListeners(events);

		dispose();
	}

	@Override
	public void dispose()
	{
		myDataSources.clear();

		myManager.resetModel();
	}
}
