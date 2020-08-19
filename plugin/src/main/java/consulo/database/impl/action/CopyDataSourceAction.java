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
