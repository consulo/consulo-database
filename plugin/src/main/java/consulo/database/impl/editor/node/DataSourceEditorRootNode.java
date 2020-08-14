package consulo.database.impl.editor.node;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.util.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.EditableDataSource;
import consulo.database.impl.toolWindow.node.DatabaseProviderNode;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceEditorRootNode extends AbstractTreeNode<Object>
{
	private final List<EditableDataSource> myEditableDataSources;

	public DataSourceEditorRootNode(Project project, List<EditableDataSource> editableDataSources)
	{
		super(project, ObjectUtil.NULL);
		myEditableDataSources = editableDataSources;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		return DatabaseProviderNode.split(getProject(), myEditableDataSources);
	}

	@Override
	protected void update(PresentationData presentation)
	{
	}
}
