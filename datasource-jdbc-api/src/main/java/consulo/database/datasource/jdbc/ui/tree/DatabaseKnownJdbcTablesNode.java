package consulo.database.datasource.jdbc.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.jdbc.provider.impl.JdbcDatabaseState;
import consulo.database.datasource.jdbc.provider.impl.JdbcState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.jdbc.provider.impl.JdbcTablesState;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransportManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class DatabaseKnownJdbcTablesNode extends AbstractTreeNode<String>
{
	private final DataSource myDataSource;

	public DatabaseKnownJdbcTablesNode(Project project, DataSource dataSource, String databaseName)
	{
		super(project, databaseName);
		myDataSource = dataSource;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		JdbcState dataState = DataSourceTransportManager.getInstance(myProject).getDataState(myDataSource);
		if(dataState == null)
		{
			return Collections.emptyList();
		}

		JdbcDatabaseState databaseState = dataState.getDatabases().get(getValue());
		if(databaseState == null || databaseState.getTablesState() == null)
		{
			return Collections.emptyList();
		}

		JdbcTablesState tablesState = databaseState.getTablesState();

		List<AbstractTreeNode> nodes = new ArrayList<>();

		List<JdbcTableState> tables = tablesState.getTables();

		for(JdbcTableState table : tables)
		{
			nodes.add(new DatabaseJdbcTableNode(myProject, table));
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
