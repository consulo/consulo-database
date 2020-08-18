package consulo.database.datasource.jdbc.ui;

import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.jdbc.ui.tree.DatabaseJdbcTablesNode;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.ui.DataSourceTreeNodeProvider;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class JdbcDataSourceTreeNodeProvider implements DataSourceTreeNodeProvider
{
	@Override
	public void fillTreeNodes(@Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull Consumer<AbstractTreeNode<?>> consumer)
	{
		consumer.accept(new DatabaseJdbcTablesNode(project, dataSource));
	}
}
