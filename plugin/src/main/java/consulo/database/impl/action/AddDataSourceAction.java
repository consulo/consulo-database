package consulo.database.impl.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import consulo.database.impl.editor.DataSourcesDialog;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class AddDataSourceAction extends DumbAwareAction
{
	public AddDataSourceAction()
	{
		super("Add Datasource", null, AllIcons.General.Add);
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

		DataSourcesDialog dialog = new DataSourcesDialog(project, null);
		dialog.showAsync();
	}
}
