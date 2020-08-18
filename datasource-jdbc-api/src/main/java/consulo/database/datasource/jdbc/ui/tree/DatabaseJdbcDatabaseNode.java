package consulo.database.datasource.jdbc.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.jdbc.provider.impl.JdbcDatabaseState;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class DatabaseJdbcDatabaseNode extends AbstractTreeNode<JdbcDatabaseState>
{
	public DatabaseJdbcDatabaseNode(Project project, JdbcDatabaseState state)
	{
		super(project, state);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		return Collections.singletonList(new DatabaseJdbcTablesNode(myProject, getValue()));
	}

	@Override
	protected void update(PresentationData presentationData)
	{
		presentationData.setIcon(AllIcons.Nodes.DataSchema);
		presentationData.addText(getValue().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
	}
}
