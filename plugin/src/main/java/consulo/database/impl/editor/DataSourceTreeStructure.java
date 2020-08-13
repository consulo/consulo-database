package consulo.database.impl.editor;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import consulo.database.impl.editor.node.DataSourceEditorRootNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceTreeStructure extends AbstractTreeStructureBase
{
	public DataSourceTreeStructure(Project project)
	{
		super(project);
	}

	@Nullable
	@Override
	public List<TreeStructureProvider> getProviders()
	{
		return null;
	}

	@Nonnull
	@Override
	public Object getRootElement()
	{
		return new DataSourceEditorRootNode(myProject);
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
