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

package consulo.database.impl.editor;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.database.datasource.editor.DataSourceEditorManager;
import consulo.database.datasource.model.DataSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
@Singleton
public class DataSourceEditorManagerImpl implements DataSourceEditorManager
{
	private final Project myProject;

	@Inject
	public DataSourceEditorManagerImpl(Project project)
	{
		myProject = project;
	}

	@Override
	public void openEditor(@Nonnull DataSource dataSource, @Nonnull String dbName, @Nonnull String childId)
	{
		VirtualFile file = DataSourceVirtualFileSystem.getInstance().createFile(dataSource, dbName, childId);

		FileEditorManager.getInstance(myProject).openFile(file, true);
	}
}
