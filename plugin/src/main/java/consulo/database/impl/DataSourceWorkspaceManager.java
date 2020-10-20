package consulo.database.impl;

import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
@Singleton
@State(name = "DataSourceWorkspaceManager", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
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
