package consulo.database.impl.toolWindow.node;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public class DatabaseTablesNode extends AbstractTreeNode<DataSource>
{
	public DatabaseTablesNode(Project project, DataSource dataSource)
	{
		super(project, dataSource);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		return Collections.emptyList();
	}

	@Override
	protected void update(PresentationData presentation)
	{
		presentation.setIcon(AllIcons.Nodes.DataTables);
		presentation.addText("Tables", SimpleTextAttributes.REGULAR_ATTRIBUTES);
	}
}
