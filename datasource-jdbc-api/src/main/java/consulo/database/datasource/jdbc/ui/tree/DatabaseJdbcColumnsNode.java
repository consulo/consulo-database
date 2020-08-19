package consulo.database.datasource.jdbc.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableColumState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DatabaseJdbcColumnsNode extends AbstractTreeNode<JdbcTableState>
{
	public DatabaseJdbcColumnsNode(Project project, @Nonnull JdbcTableState value)
	{
		super(project, value);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		List<AbstractTreeNode> columns = new ArrayList<>();
		for(JdbcTableColumState state : getValue().getColumns())
		{
			columns.add(new DatabaseJdbcColumnNode(myProject, state));
		}
		return columns;
	}

	@Override
	protected void update(PresentationData presentationData)
	{
		presentationData.setIcon(AllIcons.Nodes.Folder);
		presentationData.addText("Columns", SimpleTextAttributes.REGULAR_ATTRIBUTES);
	}
}
