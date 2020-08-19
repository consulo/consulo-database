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

package consulo.database.impl.toolWindow;

import com.intellij.openapi.actionSystem.AnSeparator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import consulo.database.impl.action.AddDataSourceAction;
import consulo.database.impl.action.EditDataSourceAction;
import consulo.database.impl.action.RefreshDataSourcesAction;
import consulo.database.impl.action.RemoveDataSourceAction;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-06-01
 */
public class DatabaseToolWindowFactory implements ToolWindowFactory, DumbAware
{
	public static final String ID = "Database";

	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		ToolWindowEx toolWindowEx = (ToolWindowEx) toolWindow;

		toolWindowEx.setTitleActions(new AddDataSourceAction(), new RemoveDataSourceAction(null), new EditDataSourceAction(), AnSeparator.create(), new RefreshDataSourcesAction());

		ContentManager contentManager = toolWindow.getContentManager();

		ContentFactory factory = contentManager.getFactory();

		DatabaseTreePanel panel = new DatabaseTreePanel(project);

		Content content = factory.createContent(panel.getRootPanel(), null, false);

		content.setDisposer(panel);

		contentManager.addContent(content);
	}
}
