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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceListener;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.model.EditableDataSourceModel;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public interface DataSourceManager
{
	Topic<DataSourceListener> TOPIC = Topic.create("datasource change listener", DataSourceListener.class);

	@Nonnull
	public static DataSourceManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, DataSourceManager.class);
	}

	@Nonnull
	@RequiredReadAction
	default List<? extends DataSource> getDataSources()
	{
		 return getModel().getDataSources();
	}

	/**
	 * @return readonly datasource model
	 */
	@Nonnull
	@RequiredReadAction
	DataSourceModel getModel();

	@Nonnull
	EditableDataSourceModel createEditableModel();
}
