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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ReadAction;
import consulo.codeEditor.Editor;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.impl.editor.DataSourceFileEditor;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.project.ui.view.MessageView;
import consulo.project.ui.wm.ToolWindowId;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.ui.Component;
import consulo.ui.NotificationType;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.content.Content;
import consulo.ui.ex.content.ContentManager;
import consulo.ui.layout.DockLayout;
import consulo.util.lang.ControlFlowException;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-10-31
 */
public class SqlExecutePanel {
    private final DockLayout myRootLayout;

    // read by DataSourceChooseAction#update on a background thread
    private volatile @Nullable UUID myDataSourceId;

    @RequiredUIAccess
    public SqlExecutePanel(Project project, Editor editor, String fileName) {
        DataSourceManager dataSourceManager = DataSourceManager.getInstance(project);

        // the first data source which can run SQL - document data sources are not offered by the chooser
        for (DataSource dataSource : dataSourceManager.getDataSources()) {
            if (DataSourceChooseAction.isSqlConsoleDataSource(dataSource)) {
                myDataSourceId = dataSource.getId();
                break;
            }
        }

        ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
        builder.add(new DumbAwareAction(LocalizeValue.localizeTODO("Execute"), LocalizeValue.empty(), PlatformIconGroup.actionsExecute()) {
            @RequiredUIAccess
            @Override
            public void actionPerformed(AnActionEvent e) {
                UUID dataSourceId = myDataSourceId;
                if (dataSourceId == null) {
                    return;
                }

                DataSource dataSource = ReadAction.compute(() -> dataSourceManager.findDataSource(dataSourceId));
                if (dataSource == null || !DataSourceChooseAction.isSqlConsoleDataSource(dataSource)) {
                    return;
                }

                execute(project, editor, fileName, dataSource);
            }
        });

        builder.add(new DataSourceChooseAction(dataSourceManager, () -> myDataSourceId, it -> myDataSourceId = it));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("SqlExecute", builder.build(), true);
        toolbar.setTargetUIComponent(editor.getUIComponent());

        myRootLayout = DockLayout.create();
        myRootLayout.left(toolbar.getUIComponent());
        myRootLayout.borderBuilder().bottomSet().apply();
    }

    @RequiredUIAccess
    private static void execute(Project project, Editor editor, String fileName, DataSource dataSource) {
        UIAccess uiAccess = UIAccess.current();

        DataSourceTransportManager transportManager = DataSourceTransportManager.getInstance(project);

        String text = editor.getDocument().getText();

        // done and rejected (with or without a throwable) both end here - on the ui thread
        transportManager.runQuery(dataSource, text).toCompletableFuture().whenCompleteAsync((result, error) -> {
            if (error != null) {
                showError(project, error);
            }
            else {
                showResult(project, uiAccess, fileName, dataSource, result);
            }
        }, uiAccess);
    }

    @RequiredUIAccess
    private static void showError(Project project, Throwable error) {
        if (error instanceof ControlFlowException) {
            // canceled by user
            return;
        }

        String message = error.getMessage();
        if (StringUtil.isEmpty(message)) {
            message = error.getClass().getSimpleName();
        }

        String balloonText = message;
        MessageView messageView = MessageView.getInstance(project);
        messageView.runWhenInitialized(() -> {
            ToolWindowManager.getInstance(project).notifyByBalloon(ToolWindowId.MESSAGES_WINDOW, NotificationType.ERROR, balloonText);
        });
    }

    /**
     * Adds a tab to the Messages tool window for every part of the result - for every result set of a script, or one tab with the
     * update counts when there is no result set.
     */
    @RequiredUIAccess
    private static void showResult(Project project, UIAccess uiAccess, String fileName, DataSource dataSource, @Nullable Object result) {
        MessageView messageView = MessageView.getInstance(project);

        messageView.runWhenInitialized(() -> {
            ContentManager contentManager = messageView.getContentManager();

            List<?> parts = DataSourceFileEditor.splitResult(result, project, dataSource);
            LocalDateTime time = LocalDateTime.now();

            @Nullable Content firstContent = null;
            for (int i = 0; i < parts.size(); i++) {
                Disposable uiDisposable = Disposable.newDisposable();

                Component component = DataSourceFileEditor.buildUI(parts.get(i), project, dataSource, null, null, uiDisposable);

                String title = parts.size() == 1 ? fileName + ": " + time : fileName + " (" + (i + 1) + "/" + parts.size() + "): " + time;
                Content content = contentManager.getFactory().createUIContent(component, title, true);
                content.setDisposer(uiDisposable);

                contentManager.addContent(content);
                if (firstContent == null) {
                    firstContent = content;
                }
            }

            if (firstContent != null) {
                contentManager.setSelectedContent(firstContent);
            }

            uiAccess.give(() -> messageView.getToolWindow().activate(null));
        });
    }

    public Component getUIComponent() {
        return myRootLayout;
    }
}
