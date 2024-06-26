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

package consulo.database.mongo;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ConfigurationException;
import consulo.configurable.UnnamedConfigurable;
import consulo.database.datasource.json.JsonDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.icon.DatabaseIconGroup;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.ui.layout.DockLayout;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
@ExtensionImpl
public class MongoDbDataSourceProvider implements JsonDataSourceProvider
{
	@Nonnull
	@Override
	public String getId()
	{
		return "mongodb";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("MongoDB");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return DatabaseIconGroup.providersMongodb();
	}

	@Nonnull
	@Override
	public UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource)
	{
		return new UnnamedConfigurable()
		{
			@RequiredUIAccess
			@Nullable
			@Override
			public Component createUIComponent(@Nonnull Disposable uiDisposable)
			{
				return DockLayout.create().center(Label.create("mongo db test"));
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
