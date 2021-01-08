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

package consulo.database.impl.sql;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-10-31
 */
public class SqlExecutePanel
{
	private JPanel myPanel = new JPanel(new BorderLayout());

	private UUID myDataSourceId;

	@RequiredReadAction
	public SqlExecutePanel(@Nonnull Project project)
	{
		DataSourceManager dataSourceManager = DataSourceManager.getInstance(project);

		List<? extends DataSource> dataSources = dataSourceManager.getDataSources();
		if(!dataSources.isEmpty())
		{
			myDataSourceId = ContainerUtil.getFirstItem(dataSources).getId();
		}

		ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
		builder.add(new AnAction("Execute", null, PlatformIconGroup.actionsExecute())
		{
			@RequiredUIAccess
			@Override
			public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
			{

			}
		});

		builder.add(new ComboBoxAction()
		{
			@Nonnull
			@Override
			@RequiredReadAction
			protected ActionGroup createPopupActionGroup(JComponent button)
			{
				ActionGroup.Builder itemBuild = ActionGroup.newImmutableBuilder();
				for(DataSource dataSource : dataSourceManager.getDataSources())
				{
					itemBuild.add(new AnAction(dataSource.getName(), "", dataSource.getProvider().getIcon())
					{
						@RequiredUIAccess
						@Override
						public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
						{
							myDataSourceId = dataSource.getId();
						}
					});
				}
				return itemBuild.build();
			}

			@RequiredUIAccess
			@Override
			public void update(@Nonnull AnActionEvent e)
			{
				Presentation presentation = e.getPresentation();
				DataSource source = myDataSourceId == null ? null : dataSourceManager.findDataSource(myDataSourceId);
				if(source == null)
				{
					presentation.setIcon(null);
					presentation.setTextValue(LocalizeValue.of("<Select DataSource>"));
				}
				else
				{
					presentation.setTextValue(LocalizeValue.of(source.getName()));
					presentation.setIcon(source.getProvider().getIcon());
				}
			}
		});

		ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("SqlExecute", builder.build(), true);

		myPanel.add(toolbar.getComponent(), BorderLayout.WEST);
	}

	public JPanel getPanel()
	{
		return myPanel;
	}
}
