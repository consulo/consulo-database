package consulo.database.impl.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class RefreshDataSourcesAction extends DumbAwareAction
{
	public RefreshDataSourcesAction()
	{
		super("Refresh Data Sources", null, AllIcons.Actions.Refresh);
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

		DataSourceTransportManager transportManager = DataSourceTransportManager.getInstance(project);

		transportManager.refreshAll();
	}
}
