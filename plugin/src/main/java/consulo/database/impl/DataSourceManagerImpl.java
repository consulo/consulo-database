package consulo.database.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceEvent;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.model.EditableDataSourceModel;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.configurable.PropertiesHolderImpl;
import consulo.database.impl.model.DataSourceImpl;
import consulo.database.impl.model.DataSourceModelImpl;
import consulo.database.impl.model.EditableDataSourceModelImpl;
import consulo.database.impl.model.UnknownDataSourceProvider;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
@Singleton
@State(name = "DataSourceManagerImpl", storages = @Storage("datasource.xml"))
public class DataSourceManagerImpl implements DataSourceManager, PersistentStateComponent<Element>
{
	private final Project myProject;

	private DataSourceModelImpl<DataSourceImpl> myModel = new DataSourceModelImpl<>();

	private EditableDataSourceModel myEditableDataSourceModel;

	@Inject
	public DataSourceManagerImpl(Project project)
	{
		myProject = project;
	}

	public void notifyListeners(@Nonnull List<DataSourceEvent> events)
	{
		for(DataSourceEvent event : events)
		{
			myProject.getMessageBus().syncPublisher(TOPIC).dataSourceEvent(event);
		}
	}

	@Nullable
	@Override
	public Element getState()
	{
		Element state = new Element("state");
		for(DataSource source : myModel.getDataSources())
		{
			Element dataSourceElement = new Element("datasource");
			dataSourceElement.setAttribute("id", source.getId());
			dataSourceElement.setAttribute("name", source.getName());
			dataSourceElement.setAttribute("provider", source.getProvider().getId());

			PropertiesHolderImpl h = (PropertiesHolderImpl) source.getProperties();

			dataSourceElement.addContent(h.toXmlState());

			state.addContent(dataSourceElement);
		}
		return state;
	}

	@Override
	public void loadState(Element state)
	{
		List<DataSourceImpl> dataSources = new ArrayList<>();

		for(Element element : state.getChildren())
		{
			String id = element.getAttributeValue("id");
			String name = element.getAttributeValue("name");
			String provider = element.getAttributeValue("provider");
			if(id == null || name == null || provider == null)
			{
				continue;
			}

			DataSourceProvider dataSourceProvider = findProvider(provider);

			DataSourceImpl dataSource = new DataSourceImpl(id, name, dataSourceProvider, myModel);

			Element propertiesElement = element.getChild(PropertiesHolderImpl.TAG_NAME);
			if(propertiesElement != null)
			{
				PropertiesHolderImpl h = (PropertiesHolderImpl) dataSource.getProperties();

				h.fromXmlState(propertiesElement);
			}

			dataSources.add(dataSource);
		}

		myModel.replaceAll(dataSources);
	}

	@Nonnull
	private DataSourceProvider findProvider(String id)
	{
		for(DataSourceProvider provider : DataSourceProvider.EP_NAME.getExtensionList())
		{
			if(id.equals(provider.getId()))
			{
				return provider;
			}
		}

		return new UnknownDataSourceProvider(id);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DataSourceModel getModel()
	{
		myProject.getApplication().assertReadAccessAllowed();

		return myModel;
	}

	@Nonnull
	@Override
	public EditableDataSourceModel createEditableModel()
	{
		if(myEditableDataSourceModel != null)
		{
			throw new IllegalArgumentException("already created");
		}

		myEditableDataSourceModel = new EditableDataSourceModelImpl(this, myModel);
		return myEditableDataSourceModel;
	}

	public void resetModel()
	{
		myEditableDataSourceModel = null;
	}
}
