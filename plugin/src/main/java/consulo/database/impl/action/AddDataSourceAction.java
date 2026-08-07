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
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.model.EditableDataSourceModel;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.configurable.editor.DatabaseSourcesDialogDescriptor;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.DumbAwareActionGroup;
import consulo.ui.ex.dialog.Dialog;
import consulo.ui.ex.dialog.DialogService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class AddDataSourceAction extends DumbAwareActionGroup {
    private static class Child extends DumbAwareAction {
        private final DataSourceProvider myProvider;

        public Child(DataSourceProvider provider) {
            super(provider.getName(), provider.getName(), provider.getIcon());
            myProvider = provider;
        }

        @RequiredUIAccess
        @Override
        public void actionPerformed(AnActionEvent e) {
            Project project = e.getRequiredData(Project.KEY);

            EditableDataSourceModel editableModel = DataSourceManager.getInstance(project).createEditableModel();

            EditableDataSource newDataSource = editableModel.newDataSource("New " + myProvider.getName() + " Connection", myProvider);

            DialogService dialogService = project.getApplication().getInstance(DialogService.class);

            Dialog dialog = dialogService.build(project, new DatabaseSourcesDialogDescriptor(project, editableModel, newDataSource));

            dialog.showAsync();
        }
    }

    private final Application myApplication;

    public AddDataSourceAction(Application application) {
        super("Add Datasource", null, PlatformIconGroup.generalAdd());
        myApplication = application;
        
        setPopup(true);
    }

    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {
        List<Child> children = new ArrayList<>();
        myApplication.getExtensionPoint(DataSourceProvider.class).forEach(provider -> children.add(new Child(provider)));
        return children.toArray(AnAction[]::new);
    }
}
