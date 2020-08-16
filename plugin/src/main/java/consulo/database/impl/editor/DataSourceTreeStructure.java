package consulo.database.impl.editor;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.model.DataSourceModel;
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
	private final DataSourceModel myDataSourceModel;
	private final DataSourceEditorRootNode myRoot;

	public DataSourceTreeStructure(Project project, DataSourceModel dataSourceModel)
	{
		super(project);
		myDataSourceModel = dataSourceModel;
		myRoot = new DataSourceEditorRootNode(myProject, dataSourceModel);
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
		return myRoot;
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
