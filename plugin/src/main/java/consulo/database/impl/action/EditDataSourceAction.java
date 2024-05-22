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

import consulo.application.AllIcons;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.configurable.editor.DataSourcesDialog;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;

import jakarta.annotation.Nonnull;

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
		Project project = e.getData(Project.KEY);
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
