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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.MessageView;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.impl.editor.DataSourceFileEditor;
import consulo.disposer.Disposable;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.concurrent.AsyncResult;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
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
	public SqlExecutePanel(@Nonnull Project project, Editor editor, String fileName)
	{
		DataSourceManager dataSourceManager = DataSourceManager.getInstance(project);

		List<? extends DataSource> dataSources = dataSourceManager.getDataSources();
		if(!dataSources.isEmpty())
		{
			myDataSourceId = ContainerUtil.getFirstItem(dataSources).getId();
		}

		ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
		builder.add(new DumbAwareAction("Execute", null, PlatformIconGroup.actionsExecute())
		{
			@RequiredUIAccess
			@Override
			public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
			{
				UIAccess uiAccess = UIAccess.current();

				DataSourceTransportManager transportManager = DataSourceTransportManager.getInstance(project);

				DataSource dataSource = dataSourceManager.findDataSource(myDataSourceId);

				assert dataSource != null;

				String text = editor.getDocument().getText();

				AsyncResult<Object> query = transportManager.runQuery(dataSource, text);
				query.doWhenRejectedWithThrowable(e ->
				{
					uiAccess.give(() ->
					{
						MessageView messageView = MessageView.getInstance(project);

						messageView.runWhenInitialized(() ->
						{
							ToolWindowManager.getInstance(project).notifyByBalloon(ToolWindowId.MESSAGES_WINDOW, MessageType.ERROR, e.getMessage());
						});
					});
				});
				query.doWhenDone((result) ->
				{
					uiAccess.give(() ->
					{
						MessageView messageView = MessageView.getInstance(project);

						messageView.runWhenInitialized(() ->
						{
							ContentManager contentManager = messageView.getContentManager();

							Disposable uiDisposable = Disposable.newDisposable();

							JComponent component = DataSourceFileEditor.buildUI(result, project, dataSource, null, null, uiDisposable);
							Content content = contentManager.getFactory().createContent(component, fileName + ": " + LocalDateTime.now(), true);
							content.setDisposer(uiDisposable);

							contentManager.addContent(content);
							contentManager.setSelectedContent(content);

							uiAccess.give(() -> messageView.getToolWindow().activate(null));
						});
					});
				});
			}
		});

		builder.add(new DataSourceChooseAction(dataSourceManager, () -> myDataSourceId, it -> myDataSourceId = it));

		ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("SqlExecute", builder.build(), true);
		toolbar.setTargetComponent(editor.getComponent());

		myPanel.add(toolbar.getComponent(), BorderLayout.WEST);
		myPanel.setBorder(JBUI.Borders.customLine(UIUtil.getBorderColor(), 0, 0, 1, 0));
	}

	public JPanel getPanel()
	{
		return myPanel;
	}
}
