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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.database.icon.DatabaseIconGroup;
import consulo.database.impl.action.AddDataSourceAction;
import consulo.database.impl.action.EditDataSourceAction;
import consulo.database.impl.action.RefreshDataSourcesAction;
import consulo.database.impl.action.RemoveDataSourceAction;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowFactory;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.awt.SimpleToolWindowPanel;
import consulo.ui.ex.content.Content;
import consulo.ui.ex.content.ContentFactory;
import consulo.ui.ex.content.ContentManager;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-06-01
 */
@ExtensionImpl
public class DatabaseToolWindowFactory implements ToolWindowFactory, DumbAware
{
	public static final String ID = "Database";

	@Nonnull
	@Override
	public ToolWindowAnchor getAnchor()
	{
		return ToolWindowAnchor.RIGHT;
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return DatabaseIconGroup.toolwindowdatabase();
	}

	@Nonnull
	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO("Database");
	}


	@Nonnull
	@Override
	public String getId()
	{
		return ID;
	}

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
