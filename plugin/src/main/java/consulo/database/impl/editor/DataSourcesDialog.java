package consulo.database.impl.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import consulo.ui.WholeWestDialogWrapper;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourcesDialog extends WholeWestDialogWrapper
{
	@Nonnull
	private final Project myProject;

	public DataSourcesDialog(@Nonnull Project project)
	{
		super(project);
		myProject = project;

		setTitle("Edit DataSources");

		init();
	}

	@Nullable
	@Override
	protected String getDimensionServiceKey()
	{
		return DataSourcesDialog.class.getName();
	}

	@Override
	public Dimension getDefaultSize()
	{
		return new Dimension(700, 500);
	}

	@RequiredUIAccess
	@Nonnull
	@Override
	public Couple<JComponent> createSplitterComponents(JPanel rootPanel)
	{
		DataSourceTreeStructure structure = new DataSourceTreeStructure(myProject);

		Tree tree = new Tree(new AsyncTreeModel(new StructureTreeModel<>(structure, myProject), myProject));
		tree.setRootVisible(false);

		return Couple.of(tree, new JPanel());
	}
}
