package consulo.database.impl.editor.node;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.util.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSource;
import consulo.database.datasource.DataSourceManager;
import consulo.database.impl.toolWindow.node.DatabaseSourceNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceEditorRootNode extends AbstractTreeNode<Object>
{
	public DataSourceEditorRootNode(Project project)
	{
		super(project, ObjectUtil.NULL);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		DataSourceManager dataSourceManager = DataSourceManager.getInstance(myProject);
		List<AbstractTreeNode> list = new ArrayList<>();
		for(DataSource source : dataSourceManager.getDataSources())
		{
			list.add(new DatabaseSourceNode(myProject, source));
		}
		return list;
	}

	@Override
	protected void update(PresentationData presentation)
	{
	}
}
