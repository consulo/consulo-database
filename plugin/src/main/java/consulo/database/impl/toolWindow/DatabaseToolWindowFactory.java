package consulo.database.impl.toolWindow;

import com.intellij.openapi.actionSystem.AnSeparator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import consulo.database.impl.action.AddDataSourceAction;
import consulo.database.impl.action.EditDataSourceAction;
import consulo.database.impl.action.RefreshDataSourcesAction;
import consulo.database.impl.action.RemoveDataSourceAction;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-06-01
 */
public class DatabaseToolWindowFactory implements ToolWindowFactory, DumbAware
{
	public static final String ID = "Database";

	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		ToolWindowEx toolWindowEx = (ToolWindowEx) toolWindow;

		toolWindowEx.setTitleActions(new AddDataSourceAction(), new RemoveDataSourceAction(null), new EditDataSourceAction(), AnSeparator.create(), new RefreshDataSourcesAction());

		ContentManager contentManager = toolWindow.getContentManager();

		ContentFactory factory = contentManager.getFactory();

		DatabaseTreePanel panel = new DatabaseTreePanel(project);

		Content content = factory.createContent(panel.getRootPanel(), null, false);

		content.setDisposer(panel);

		contentManager.addContent(content);
	}
}
