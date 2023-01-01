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

package consulo.database.datasource;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.model.EditableDataSourceModel;
import consulo.project.Project;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
@ServiceAPI(ComponentScope.PROJECT)
public interface DataSourceManager
{
	public static DataSourceManager getInstance(@Nonnull Project project)
	{
		return project.getInstance(DataSourceManager.class);
	}

	@RequiredReadAction
	default List<? extends DataSource> getDataSources()
	{
		 return getModel().getDataSources();
	}

	@RequiredReadAction
	default DataSource findDataSource(@Nonnull UUID uuid)
	{
		return getModel().findDataSource(uuid);
	}

	/**
	 * @return readonly datasource model
	 */
	@RequiredReadAction
	DataSourceModel getModel();

	EditableDataSourceModel createEditableModel();
}
