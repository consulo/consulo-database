/*
 * Copyright 2013-2021 consulo.io
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
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.fileEditor.util.FileContentUtil;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.version.LanguageVersion;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.sql.language.SqlFileType;
import consulo.sql.language.SqlLanguage;
import consulo.sql.language.psi.SqlFile;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.awt.action.ComboBoxAction;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;

import javax.swing.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2021-01-31
 */
public class DataSourceChooseAction extends ComboBoxAction {
    private final DataSourceManager myDataSourceManager;
    private final Supplier<UUID> myGetter;
    private final Consumer<UUID> mySetter;

    public DataSourceChooseAction(DataSourceManager dataSourceManager, Supplier<UUID> getter, Consumer<UUID> setter) {
        myDataSourceManager = dataSourceManager;
        myGetter = getter;
        mySetter = setter;
    }

    @Nonnull
    @Override
    @RequiredReadAction
    protected ActionGroup createPopupActionGroup(JComponent button) {
        ActionGroup.Builder itemBuild = ActionGroup.newImmutableBuilder();
        for (DataSource dataSource : myDataSourceManager.getDataSources()) {
            itemBuild.add(new DumbAwareAction(dataSource.getName(), "", dataSource.getProvider().getIcon()) {
                @RequiredUIAccess
                @Override
                public void actionPerformed(@Nonnull AnActionEvent e) {
                    mySetter.accept(dataSource.getId());

                    updatePresentation(DataSourceChooseAction.this.getTemplatePresentation());

                    VirtualFile file = e.getData(VirtualFile.KEY);
                    Project project = e.getRequiredData(Project.KEY);
                    Class<? extends LanguageVersion> sqlDialect = dataSource.getProvider().getSqlDialect();

                    if (file != null && sqlDialect != null && file.getFileType() == SqlFileType.INSTANCE) {
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                        if (psiFile instanceof SqlFile sqlFile) {
                            LanguageVersion version = SqlLanguage.INSTANCE.findVersionByClass(sqlDialect);

                            sqlFile.putUserData(LanguageVersion.KEY, version);

                            file.putUserData(LanguageVersion.KEY, version);

                            FileContentUtil.reparseFiles(project, List.of(file), true);
                        }
                    }
                }
            });
        }
        return itemBuild.build();
    }

    @Override
    @RequiredUIAccess
    public void update(@Nonnull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        updatePresentation(presentation);
    }

    @RequiredUIAccess
    protected void updatePresentation(Presentation presentation) {
        UUID dataSourceId = myGetter.get();

        DataSource source = dataSourceId == null ? null : ReadAction.compute(() -> myDataSourceManager.findDataSource(dataSourceId));
        if (source == null) {
            presentation.setIcon(null);
            presentation.setText(LocalizeValue.localizeTODO("<Select DataSource>"));
        }
        else {
            presentation.setText(LocalizeValue.of(source.getName()));
            presentation.setIcon(source.getProvider().getIcon());
        }
    }
}
