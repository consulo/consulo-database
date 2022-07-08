/*
 * Copyright 2013-2021 consulo.io
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

import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.localize.LocalizeValue;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.awt.action.ComboBoxAction;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 31/01/2021
 */
public class DataSourceChooseAction extends ComboBoxAction
{
	private final DataSourceManager myDataSourceManager;
	private final Supplier<UUID> myGetter;
	private final Consumer<UUID> mySetter;

	public DataSourceChooseAction(DataSourceManager dataSourceManager, Supplier<UUID> getter, Consumer<UUID> setter)
	{
		myDataSourceManager = dataSourceManager;
		myGetter = getter;
		mySetter = setter;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	protected ActionGroup createPopupActionGroup(JComponent button)
	{
		ActionGroup.Builder itemBuild = ActionGroup.newImmutableBuilder();
		for(DataSource dataSource : myDataSourceManager.getDataSources())
		{
			itemBuild.add(new DumbAwareAction(dataSource.getName(), "", dataSource.getProvider().getIcon())
			{
				@RequiredUIAccess
				@Override
				public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
				{
					mySetter.accept(dataSource.getId());

					updatePresentation(DataSourceChooseAction.this.getTemplatePresentation());
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
		updatePresentation(presentation);
	}

	@RequiredUIAccess
	protected void updatePresentation(Presentation presentation)
	{
		UUID dataSourceId = myGetter.get();

		DataSource source = dataSourceId == null ? null : myDataSourceManager.findDataSource(dataSourceId);
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
}
