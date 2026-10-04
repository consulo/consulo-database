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

import consulo.application.ReadAction;
import consulo.dataContext.DataContext;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.json.JsonDataSourceProvider;
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
import consulo.ui.ex.action.ComboBoxAction;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.Presentation;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

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
    private final Supplier<@Nullable UUID> myGetter;
    private final Consumer<UUID> mySetter;

    public DataSourceChooseAction(DataSourceManager dataSourceManager, Supplier<@Nullable UUID> getter, Consumer<UUID> setter) {
        myDataSourceManager = dataSourceManager;
        myGetter = getter;
        mySetter = setter;
    }

    /**
     * The console sends SQL text - so document data sources (MongoDB and so on) are not offered
     */
    public static boolean isSqlConsoleDataSource(DataSource dataSource) {
        return !(dataSource.getProvider() instanceof JsonDataSourceProvider);
    }

    @Override
    @RequiredUIAccess
    protected ActionGroup createPopupActionGroup(DataContext context) {
        List<? extends DataSource> dataSources = ReadAction.compute(myDataSourceManager::getDataSources);

        ActionGroup.Builder itemBuild = ActionGroup.newImmutableBuilder();
        for (DataSource dataSource : dataSources) {
            if (!isSqlConsoleDataSource(dataSource)) {
                continue;
            }

            LocalizeValue text = LocalizeValue.of(dataSource.getName());

            itemBuild.add(new DumbAwareAction(text, LocalizeValue.empty(), dataSource.getProvider().getIcon()) {
                @RequiredUIAccess
                @Override
                public void actionPerformed(AnActionEvent e) {
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

    /**
     * Runs on a background thread (see {@link consulo.ui.ex.action.AnActionWithSyncUpdate#update})
     */
    @Override
    public void update(AnActionEvent e) {
        updatePresentation(e.getPresentation());
    }

    protected void updatePresentation(Presentation presentation) {
        UUID dataSourceId = myGetter.get();

        DataSource source = dataSourceId == null ? null : ReadAction.compute(() -> myDataSourceManager.findDataSource(dataSourceId));
        if (source == null || !isSqlConsoleDataSource(source)) {
            presentation.setIcon(null);
            presentation.setText(LocalizeValue.localizeTODO("<Select DataSource>"));
        }
        else {
            presentation.setText(LocalizeValue.of(source.getName()));
            presentation.setIcon(source.getProvider().getIcon());
        }
    }
}
