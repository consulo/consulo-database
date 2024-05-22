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
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSourceModel;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
		Project project = e.getData(Project.KEY);
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
			model.removeDataSource(source.getId());
			model.commit();
		}
		else
		{
			// do not commit if we have editable model
			myModel.removeDataSource(source.getId());
		}
	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		e.getPresentation().setEnabled(e.getData(DataSourceKeys.DATASOURCE) != null);
	}
}
