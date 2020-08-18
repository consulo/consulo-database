package consulo.database.datasource.ui;

import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public interface DataSourceTreeNodeProvider
{
	ExtensionPointName<DataSourceTreeNodeProvider> EP_NAME = ExtensionPointName.create("consulo.database.treeNodeProvider");

	void fillTreeNodes(@Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull Consumer<AbstractTreeNode<?>> consumer);
}
