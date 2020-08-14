package consulo.database.impl.editor;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import consulo.database.datasource.DataSource;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.EditableDataSource;
import consulo.database.impl.action.RemoveDataSourceAction;
import consulo.database.impl.editor.action.AddDataSourcePopupAction;
import consulo.database.impl.toolWindow.node.DatabaseSourceNode;
import consulo.options.ConfigurableUIMigrationUtil;
import consulo.ui.SwingUIDecorator;
import consulo.ui.WholeWestDialogWrapper;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourcesDialog extends WholeWestDialogWrapper
{
	@Nonnull
	private final Project myProject;

	private Wrapper myConfigurableWrapper;

	private List<EditableDataSource> myDataSources;

	public DataSourcesDialog(@Nonnull Project project)
	{
		super(project);
		myProject = project;

		setTitle("Edit DataSources");

		myDataSources = new ArrayList<>();

		List<DataSource> dataSources = DataSourceManager.getInstance(project).getDataSources();

		for(DataSource dataSource : dataSources)
		{
			myDataSources.add(dataSource.wantEdit());
		}

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
		DataSourceTreeStructure structure = new DataSourceTreeStructure(myProject, myDataSources);

		StructureTreeModel<DataSourceTreeStructure> structureModel = new StructureTreeModel<>(structure, myProject);
		AsyncTreeModel model = new AsyncTreeModel(structureModel, myProject);
		Tree tree = new Tree(model);

		Runnable treeUpdater = () ->
		{
			structureModel.invalidate(structure.getRootElement(), true);
		};

		tree.addTreeSelectionListener(e ->
		{
			SwingUtilities.invokeLater(() ->
			{
				Object lastUserObject = TreeUtil.getLastUserObject(e.getPath());

				if(lastUserObject instanceof DatabaseSourceNode)
				{
					DataSource value = ((DatabaseSourceNode) lastUserObject).getValue();
					selectConfigurable((EditableDataSource) value, treeUpdater);
				}
				else
				{
					selectConfigurable(null, treeUpdater);
				}
			});
		});

		tree.setOpaque(false);
		tree.setRootVisible(false);
		TreeUtil.expandAll(tree);

		ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
		builder.add(new AddDataSourcePopupAction());
		builder.add(new RemoveDataSourceAction());

		ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DataSourceEditor", builder.build(), true);
		toolbar.setTargetComponent(tree);

		BorderLayoutPanel panel = new BorderLayoutPanel();
		panel.setBackground(SwingUIDecorator.get(SwingUIDecorator::getSidebarColor));

		JComponent component = toolbar.getComponent();
		component.setOpaque(false);
		component.setBackground(SwingUIDecorator.get(SwingUIDecorator::getSidebarColor));
		panel.addToTop(component);
		panel.addToCenter(tree);

		myConfigurableWrapper = new Wrapper();

		return Couple.of(panel, myConfigurableWrapper);
	}

	@RequiredUIAccess
	private void selectConfigurable(@Nullable EditableDataSource dataSource, Runnable treeUpdater)
	{
		if(dataSource == null)
		{
			myConfigurableWrapper.setContent(null);
		}
		else
		{
			DataSourceConfigurable c = new DataSourceConfigurable(dataSource, treeUpdater);
			c.reset();
			myConfigurableWrapper.setContent(ConfigurableUIMigrationUtil.createComponent(c));
		}
	}

	@Override
	protected void doOKAction()
	{
		for(EditableDataSource dataSource : myDataSources)
		{
			dataSource.commit();
		}

		super.doOKAction();
	}

	@Nullable
	@Override
	protected Border createContentPaneBorder()
	{
		return JBUI.Borders.empty();
	}

	@Nullable
	@Override
	protected JComponent createSouthPanel()
	{
		JComponent southPanel = super.createSouthPanel();
		if(southPanel != null)
		{
			southPanel.setBorder(JBUI.Borders.empty(ourDefaultBorderInsets));
			return JBUI.Panels.simplePanel(southPanel);
		}
		return null;
	}
}
