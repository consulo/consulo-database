package consulo.database.impl.toolWindow;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import consulo.database.impl.toolWindow.node.DatabaseRootNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseTreeStructure extends AbstractTreeStructureBase
{
	public DatabaseTreeStructure(Project project)
	{
		super(project);
	}

	@Nonnull
	@Override
	public Object getRootElement()
	{
		return new DatabaseRootNode(myProject);
	}

	@Nullable
	@Override
	public List<TreeStructureProvider> getProviders()
	{
		return null;
	}

	@Override
	public void commit()
	{

	}

	@Override
	public boolean hasSomethingToCommit()
	{
		return false;
	}
}
