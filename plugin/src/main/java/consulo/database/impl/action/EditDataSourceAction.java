package consulo.database.impl.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.editor.DataSourcesDialog;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class EditDataSourceAction extends DumbAwareAction
{
	public EditDataSourceAction()
	{
		super("Edit", null, AllIcons.Actions.Edit);
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		DataSource dataSource = e.getData(DataSourceKeys.DATASOURCE);
		Project project = e.getProject();
		if(project == null || dataSource == null)
		{
			return;
		}

		DataSourcesDialog dialog = new DataSourcesDialog(project, dataSource);
		dialog.showAsync();
	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		e.getPresentation().setEnabled(e.getData(DataSourceKeys.DATASOURCE) != null);
	}
}
