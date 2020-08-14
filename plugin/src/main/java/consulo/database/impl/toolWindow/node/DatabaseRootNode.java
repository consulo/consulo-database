package consulo.database.impl.toolWindow.node;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.util.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSourceManager;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseRootNode extends AbstractTreeNode<Object>
{
	public DatabaseRootNode(Project project)
	{
		super(project, ObjectUtil.NULL);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		DataSourceManager dataSourceManager = DataSourceManager.getInstance(myProject);
		return DatabaseProviderNode.split(getProject(), dataSourceManager.getDataSources());
	}

	@Override
	protected void update(PresentationData presentationData)
	{

	}
}
