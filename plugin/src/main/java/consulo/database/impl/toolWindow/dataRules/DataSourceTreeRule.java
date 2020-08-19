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

package consulo.database.impl.toolWindow.dataRules;

import com.intellij.ide.DataManager;
import com.intellij.ide.impl.dataRules.GetDataRule;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.toolWindow.DatabaseToolWindowFactory;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class DataSourceTreeRule implements GetDataRule
{
	@Nonnull
	@Override
	public Key getKey()
	{
		return DataSourceKeys.DATASOURCE;
	}

	@Nullable
	@Override
	public Object getData(@Nonnull DataProvider dataProvider)
	{
		ToolWindow window = dataProvider.getDataUnchecked(PlatformDataKeys.TOOL_WINDOW);
		if(window != null && DatabaseToolWindowFactory.ID.equals(window.getId()))
		{
			ContentManager contentManager = window.getContentManager();

			Content selectedContent = contentManager.getSelectedContent();

			if(selectedContent == null)
			{
				return null;
			}
			
			JComponent component = selectedContent.getComponent();

			DataProvider provider = DataManager.getDataProvider(component);

			if(provider != null)
			{
				return provider.getData(DataSourceKeys.DATASOURCE);
			}
		}
		return null;
	}
}
