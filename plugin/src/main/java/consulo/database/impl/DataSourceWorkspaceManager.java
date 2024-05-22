package consulo.database.impl;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.ide.ServiceManager;
import consulo.project.Project;
import consulo.ui.ex.awt.tree.TreeState;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdom.Element;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
@Singleton
@State(name = "DataSourceWorkspaceManager", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class DataSourceWorkspaceManager implements PersistentStateComponent<Element>
{
	public static DataSourceWorkspaceManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, DataSourceWorkspaceManager.class);
	}

	private final Project myProject;

	private TreeState myTreeState;

	@Inject
	public DataSourceWorkspaceManager(Project project)
	{
		myProject = project;
	}

	public void setTreeState(@Nullable TreeState treeState)
	{
		myTreeState = treeState;
	}

	@Nullable
	public TreeState getTreeState()
	{
		return myTreeState;
	}

	@Nullable
	@Override
	public Element getState()
	{
		Element stateElement = new Element("state");
		TreeState state = myTreeState;
		if(state != null)
		{
			Element element = new Element("tree-state");
			state.writeExternal(element);
			stateElement.addContent(element);
		}
		return stateElement;
	}

	@Override
	public void loadState(Element state)
	{
		Element treeState = state.getChild("tree-state");
		if(treeState != null)
		{
			myTreeState = TreeState.createFrom(treeState);
		}
	}
}
