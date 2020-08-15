package consulo.database.impl.editor.node;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.util.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSourceModel;
import consulo.database.impl.toolWindow.node.DatabaseProviderNode;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceEditorRootNode extends AbstractTreeNode<Object>
{
	private final DataSourceModel myDataSourceModel;

	public DataSourceEditorRootNode(Project project, DataSourceModel dataSourceModel)
	{
		super(project, ObjectUtil.NULL);
		myDataSourceModel = dataSourceModel;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		return DatabaseProviderNode.split(getProject(), myDataSourceModel);
	}

	@Override
	protected void update(PresentationData presentation)
	{
	}
}
