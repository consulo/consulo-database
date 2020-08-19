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

package consulo.database.datasource.editor;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public interface DataSourceEditorManager
{
	@Nonnull
	static DataSourceEditorManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, DataSourceEditorManager.class);
	}

	void openEditor(@Nonnull DataSource dataSource, @Nonnull String childId);
}
