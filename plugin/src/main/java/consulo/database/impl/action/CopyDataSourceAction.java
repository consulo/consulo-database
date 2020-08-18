package consulo.database.impl.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSourceModel;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class CopyDataSourceAction extends DumbAwareAction
{
	@Nonnull
	private final EditableDataSourceModel myEditableDataSourceModel;

	public CopyDataSourceAction(@Nonnull EditableDataSourceModel editableDataSourceModel)
	{
		super("Copy Datasource", null, AllIcons.Actions.Copy);
		myEditableDataSourceModel = editableDataSourceModel;
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

		myEditableDataSourceModel.newDataSourceCopy(source.getName() + " Copy", source);
	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		e.getPresentation().setEnabled(e.getData(DataSourceKeys.DATASOURCE) != null);
	}
}
