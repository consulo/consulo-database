package consulo.database.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSourceEvent;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.DataSourceModel;
import consulo.database.datasource.EditableDataSourceModel;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
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
		return state;
	}

	@Override
	public void loadState(Element state)
	{

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

	protected void resetModel()
	{
		myEditableDataSourceModel = null;
	}
}
