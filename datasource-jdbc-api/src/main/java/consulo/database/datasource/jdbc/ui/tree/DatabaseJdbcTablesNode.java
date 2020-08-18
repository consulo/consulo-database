package consulo.database.datasource.jdbc.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.jdbc.provider.impl.JdbcState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransportManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public class DatabaseJdbcTablesNode extends AbstractTreeNode<DataSource>
{
	private final DataSource myDataSource;

	public DatabaseJdbcTablesNode(Project project, DataSource dataSource)
	{
		super(project, dataSource);
		myDataSource = dataSource;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		JdbcState state = DataSourceTransportManager.getInstance(myProject).getDataState(myDataSource);

		List<AbstractTreeNode> nodes = new ArrayList<>();

		if(state != null)
		{
			List<JdbcTableState> tables = state.getTables();

			for(JdbcTableState table : tables)
			{
				nodes.add(new DatabaseJdbcTableNode(myProject, table));
			}
		}
		return nodes;
	}

	@Override
	protected void update(PresentationData presentation)
	{
		presentation.setIcon(AllIcons.Nodes.DataSource);
		presentation.addText("Tables", SimpleTextAttributes.REGULAR_ATTRIBUTES);
	}
}
