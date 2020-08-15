package consulo.database.impl.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.DataSource;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.EditableDataSourceModel;
import consulo.database.datasource.tree.DataSourceKeys;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class RemoveDataSourceAction extends DumbAwareAction
{
	@Nullable
	private final EditableDataSourceModel myModel;

	public RemoveDataSourceAction(@Nullable EditableDataSourceModel model)
	{
		super("Remove", null, AllIcons.General.Remove);
		myModel = model;
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		Project project = e.getProject();
		if(project == null)
		{
			return;
		}

		DataSource source = e.getData(DataSourceKeys.DATASOURCE);
		if(source == null)
		{
			return;
		}

		if(myModel == null)
		{
			EditableDataSourceModel model = DataSourceManager.getInstance(project).createEditableModel();
			model.removeDataSource(source.getName());
			model.commit();
		}
		else
		{
			// do not commit if we have editable model
			myModel.removeDataSource(source.getName());
		}
	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		e.getPresentation().setEnabled(e.getData(DataSourceKeys.DATASOURCE) != null);
	}
}
