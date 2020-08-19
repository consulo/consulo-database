package consulo.database.datasource.jdbc.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableColumState;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DatabaseJdbcColumnNode extends AbstractTreeNode<JdbcTableColumState>
{
	public DatabaseJdbcColumnNode(Project project, @Nonnull JdbcTableColumState value)
	{
		super(project, value);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		return Collections.emptyList();
	}

	@Override
	protected void update(PresentationData presentationData)
	{
		presentationData.setIcon(AllIcons.Nodes.DataColumn);
		presentationData.addText(getValue().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		presentationData.addText(" : " + getValue().getType(), SimpleTextAttributes.GRAY_ATTRIBUTES);
	}

	@Override
	public boolean isAlwaysLeaf()
	{
		return true;
	}
}
