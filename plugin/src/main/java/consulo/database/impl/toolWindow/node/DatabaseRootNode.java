package consulo.database.impl.toolWindow.node;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.util.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSource;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

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
		return Collections.singletonList(new DatabaseSourceNode(getProject(), new DataSource()
		{
		}));
	}

	@Override
	protected void update(PresentationData presentationData)
	{

	}
}
