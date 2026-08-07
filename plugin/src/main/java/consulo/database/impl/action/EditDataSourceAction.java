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

package consulo.database.impl.action;

import consulo.application.Application;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSourceModel;
import consulo.database.datasource.ui.DataSourceKeys;
import consulo.database.impl.configurable.editor.DatabaseSourcesDialogDescriptor;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.dialog.Dialog;
import consulo.ui.ex.dialog.DialogService;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class EditDataSourceAction extends DumbAwareAction implements AnActionWithSyncUpdate {
    public EditDataSourceAction() {
        super("Edit", null, PlatformIconGroup.actionsEdit());
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(@Nonnull AnActionEvent e) {
        DataSource dataSource = e.getData(DataSourceKeys.DATASOURCE);
        Project project = e.getData(Project.KEY);
        if (project == null || dataSource == null) {
            return;
        }

        EditableDataSourceModel editableModel = DataSourceManager.getInstance(project).createEditableModel();

        DialogService dialogService = Application.get().getInstance(DialogService.class);

        DataSource editTarget = editableModel.findDataSource(dataSource.getId());

        Dialog dialog = dialogService.build(project, new DatabaseSourcesDialogDescriptor(project, editableModel, editTarget));

        dialog.showAsync();
    }

    @Override
    public void update(@Nonnull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getData(DataSourceKeys.DATASOURCE) != null);
    }
}
