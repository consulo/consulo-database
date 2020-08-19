package consulo.database.impl.editor;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.ui.update.UiNotifyConnector;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.*;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.action.CopyDataSourceAction;
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
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourcesDialog extends WholeWestDialogWrapper
{
	@Nonnull
	private final Project myProject;
	@Nullable
	private final DataSource mySelectedDataSource;

	private Wrapper myConfigurableWrapper;

	private Configurable mySelectedConfigurable;

	private EditableDataSourceModel myEditableDataSourceModel;

	public DataSourcesDialog(@Nonnull Project project, @Nullable DataSource selectedDataSource)
	{
		super(project);
		myProject = project;
		mySelectedDataSource = selectedDataSource;

		setTitle("Edit DataSources");

		myEditableDataSourceModel = DataSourceManager.getInstance(project).createEditableModel();

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
		DataSourceTreeStructure structure = new DataSourceTreeStructure(myProject, myEditableDataSourceModel);

		StructureTreeModel<DataSourceTreeStructure> structureModel = new StructureTreeModel<>(structure, myProject);
		AsyncTreeModel model = new AsyncTreeModel(structureModel, myProject);
		Tree tree = new Tree(model);

		Runnable treeUpdater = () ->
		{
			structureModel.invalidate(structure.getRootElement(), true);
		};

		myEditableDataSourceModel.addListener(new DataSourceListener()
		{
			@Override
			public void dataSourceEvent(DataSourceEvent event)
			{
				if(event.getAction() == DataSourceEvent.Action.ADD)
				{
					structureModel.invalidate(structure.getRootElement(), true).onSuccess(treePath ->
					{
						TreeUtil.expandAll(tree);
					});

					selectConfigurable((EditableDataSource) event.getDataSource(), treeUpdater);
				}
				else if(event.getAction() == DataSourceEvent.Action.REMOVE)
				{
					structureModel.invalidate(structure.getRootElement(), true).onSuccess(treePath ->
					{
						TreeUtil.expandAll(tree);
					});

					selectConfigurable(null, treeUpdater);
				}
			}
		});

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
		builder.add(new AddDataSourcePopupAction(myEditableDataSourceModel));
		builder.add(new RemoveDataSourceAction(myEditableDataSourceModel));
		builder.add(new CopyDataSourceAction(myEditableDataSourceModel));

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

		DataManager.registerDataProvider(panel, dataId ->
		{
			if(dataId == DataSourceKeys.DATASOURCE)
			{
				TreePath path = TreeUtil.getSelectedPathIfOne(tree);
				if(path != null)
				{
					Object lastUserObject = TreeUtil.getLastUserObject(path);
					if(lastUserObject instanceof DatabaseSourceNode)
					{
						return ((DatabaseSourceNode) lastUserObject).getValue();
					}
				}
			}

			return null;
		});

		if(mySelectedDataSource != null)
		{
			DataSource dataSource = myEditableDataSourceModel.findDataSource(mySelectedDataSource.getId());

			UiNotifyConnector.doWhenFirstShown(panel, () ->
			{
				selectConfigurable((EditableDataSource) dataSource, treeUpdater);
			});
		}
		return Couple.of(panel, myConfigurableWrapper);
	}

	@RequiredUIAccess
	private void selectConfigurable(@Nullable EditableDataSource dataSource, Runnable treeUpdater)
	{
		if(mySelectedConfigurable != null)
		{
			try
			{
				mySelectedConfigurable.apply();
			}
			catch(ConfigurationException ignored)
			{
			}

			mySelectedConfigurable = null;
		}

		if(dataSource == null)
		{
			myConfigurableWrapper.setContent(null);
		}
		else
		{
			DataSourceConfigurable c = new DataSourceConfigurable(myProject, dataSource, treeUpdater);
			c.reset();

			mySelectedConfigurable = c;

			myConfigurableWrapper.setContent(ConfigurableUIMigrationUtil.createComponent(c));
		}
	}

	@Override
	@RequiredUIAccess
	protected void doOKAction()
	{
		if(mySelectedConfigurable != null)
		{
			try
			{
				mySelectedConfigurable.apply();
			}
			catch(ConfigurationException ignored)
			{
			}
		}

		myEditableDataSourceModel.commit();

		super.doOKAction();
	}

	@Override
	public void doCancelAction()
	{
		myEditableDataSourceModel.dispose();

		super.doCancelAction();
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
