package consulo.database.impl.toolWindow;

import com.intellij.ide.DataManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import consulo.database.datasource.tree.DataSourceKeys;
import consulo.disposer.Disposable;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseTreePanel implements Disposable
{
	private JPanel myRootPanel;

	public DatabaseTreePanel(@Nonnull Project project)
	{
		myRootPanel = new JPanel(new BorderLayout());

		Tree tree = new Tree(new AsyncTreeModel(new StructureTreeModel<>(new DatabaseTreeStructure(project), this), this));
		tree.setRootVisible(false);

		DataManager.registerDataProvider(myRootPanel, key ->
		{
			if(key == DataSourceKeys.TREE)
			{
				return tree;
			}
			else if(key == DataSourceKeys.DATASOURCE)
			{
				TreePath path = TreeUtil.getSelectedPathIfOne(tree);
				if(path != null)
				{
					Object lastUserObject = TreeUtil.getLastUserObject(path);
					System.out.println();
				}
			}
			return null;
		});
		myRootPanel.add(ScrollPaneFactory.createScrollPane(tree, true));
	}

	public JPanel getRootPanel()
	{
		return myRootPanel;
	}

	@Override
	public void dispose()
	{

	}
}
