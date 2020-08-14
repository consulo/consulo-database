package consulo.database.impl.editor.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import consulo.awt.TargetAWT;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public class AddDataSourcePopupAction extends DumbAwareAction
{
	private static class StepImpl extends BaseListPopupStep<DataSourceProvider>
	{
		private StepImpl()
		{
			super("Choose Data Source", DataSourceProvider.EP_NAME.getExtensionList());
		}

		@Nonnull
		@Override
		public String getTextFor(DataSourceProvider value)
		{
			return value.getName().getValue();
		}

		@Override
		public Icon getIconFor(DataSourceProvider value)
		{
			return TargetAWT.to(value.getIcon());
		}
	}

	public AddDataSourcePopupAction()
	{
		super("Add Datasource", null, AllIcons.General.Add);
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		ListPopup popup = JBPopupFactory.getInstance().createListPopup(new StepImpl());

		popup.showUnderneathOf(e.getInputEvent().getComponent());
	}
}
