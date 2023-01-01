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
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;

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
		Project project = e.getData(Project.KEY);
		if(project == null)
		{
			return;
		}

		DataSourceTransportManager transportManager = DataSourceTransportManager.getInstance(project);

		transportManager.refreshAll();
	}
}
