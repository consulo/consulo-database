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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import consulo.database.impl.action.AddDataSourceAction;
import consulo.database.impl.action.EditDataSourceAction;
import consulo.database.impl.action.RefreshDataSourcesAction;
import consulo.database.impl.action.RemoveDataSourceAction;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-06-01
 */
public class DatabaseToolWindowFactory implements ToolWindowFactory, DumbAware
{
	public static final String ID = "Database";

	@RequiredUIAccess
	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		ContentManager contentManager = toolWindow.getContentManager();

		ContentFactory factory = contentManager.getFactory();

		SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(true, true);

		DatabaseTreePanel panel = new DatabaseTreePanel(project);
		toolWindowPanel.setContent(panel.getRootPanel());

		ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
		builder.add(new AddDataSourceAction());
		builder.add(new RemoveDataSourceAction(null));
		builder.add(new EditDataSourceAction());
		builder.addSeparator();
		builder.add(new RefreshDataSourcesAction());

		ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DatabaseToolWindow", builder.build(), true);
		toolbar.setTargetComponent(panel.getRootPanel());
		toolWindowPanel.setToolbar(toolbar.getComponent());

		Content content = factory.createContent(toolWindowPanel, null, false);

		content.setDisposer(panel);

		contentManager.addContent(content);
	}
}
