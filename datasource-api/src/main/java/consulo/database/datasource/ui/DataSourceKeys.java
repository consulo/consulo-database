package consulo.database.datasource.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.treeStructure.Tree;
import consulo.database.datasource.model.DataSource;
import consulo.util.dataholder.Key;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public interface DataSourceKeys
{
	Key<Tree> TREE = Key.create("datasource.tree");

	Key<DataSource> DATASOURCE = Key.create("datasource");
}
