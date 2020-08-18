package consulo.database.impl.toolWindow.node;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.ui.DataSourceTreeNodeProvider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseSourceNode extends AbstractTreeNode<DataSource>
{
	public DatabaseSourceNode(Project project, @Nonnull DataSource value)
	{
		super(project, value);
	}

	@Override
	protected void update(PresentationData presentationData)
	{
		DataSource dataSource = getValue();

		presentationData.addText(dataSource.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		presentationData.setIcon(dataSource.getProvider().getIcon());
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		DataSource value = getValue();
		if(value instanceof EditableDataSource)
		{
			return Collections.emptyList();
		}
		List<AbstractTreeNode<?>> result = new ArrayList<>();
		for(DataSourceTreeNodeProvider provider : DataSourceTreeNodeProvider.EP_NAME.getExtensionList())
		{
			provider.fillTreeNodes(myProject, value, result::add);
		}
		return result;
	}
}
