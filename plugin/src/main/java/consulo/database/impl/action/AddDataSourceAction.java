/*
 * Copyright 2013-2020 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.database.impl.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.model.EditableDataSourceModel;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.configurable.editor.DataSourcesDialog;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class AddDataSourceAction extends DumbAwareAction
{
	private class StepImpl extends BaseListPopupStep<DataSourceProvider>
	{
		private final Project myProject;

		private StepImpl(@Nonnull Project project)
		{
			super("Choose Data Source", DataSourceProvider.EP_NAME.getExtensionList());
			myProject = project;
		}

		@Nonnull
		@Override
		public String getTextFor(DataSourceProvider value)
		{
			return value.getName().getValue();
		}

		@Override
		public Image getIconFor(DataSourceProvider value)
		{
			return value.getIcon();
		}

		@Override
		public PopupStep onChosen(DataSourceProvider selectedValue, boolean finalChoice)
		{
			return doFinalStep(() -> createAndShowDialog(selectedValue));
		}

		@RequiredUIAccess
		private void createAndShowDialog(DataSourceProvider selectedValue)
		{
			EditableDataSourceModel editableModel = DataSourceManager.getInstance(myProject).createEditableModel();

			EditableDataSource newDataSource = editableModel.newDataSource("New " + selectedValue.getName() + " Connection", selectedValue);

			DataSourcesDialog dialog = new DataSourcesDialog(myProject, editableModel, newDataSource);
			
			dialog.showAsync();
		}
	}

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

		ListPopup popup = JBPopupFactory.getInstance().createListPopup(new StepImpl(project));

		popup.showUnderneathOf(e.getInputEvent().getComponent());
	}
}
