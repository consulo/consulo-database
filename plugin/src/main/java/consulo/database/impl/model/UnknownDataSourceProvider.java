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

package consulo.database.impl.model;

import consulo.application.AllIcons;
import consulo.configurable.ConfigurationException;
import consulo.configurable.UnnamedConfigurable;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.localize.LocalizeValue;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.ui.layout.DockLayout;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class UnknownDataSourceProvider implements DataSourceProvider
{
	private final String myId;

	public UnknownDataSourceProvider(String id)
	{
		myId = id;
	}

	@Nonnull
	@Override
	public String getId()
	{
		return myId;
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of(StringUtil.capitalize(myId));
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return AllIcons.Actions.Help;
	}

	@Override
	public UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource)
	{
		return new UnnamedConfigurable()
		{
			@RequiredUIAccess
			@Override
			public Component createUIComponent()
			{
				return DockLayout.create().center(Label.create("Unknown datasource provider"));
			}

			@RequiredUIAccess
			@Override
			public boolean isModified()
			{
				return false;
			}

			@RequiredUIAccess
			@Override
			public void apply() throws ConfigurationException
			{

			}
		};
	}
}
