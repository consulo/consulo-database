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

package consulo.database.datasource.model;

import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public interface EditableDataSourceModel extends DataSourceModel
{
	void addListener(DataSourceListener listener);

	@Nonnull
	@Override
	List<? extends EditableDataSource> getDataSources();

	EditableDataSource newDataSource(@Nonnull String name, @Nonnull DataSourceProvider dataSourceProvider);

	EditableDataSource newDataSourceCopy(@Nonnull String name, @Nonnull DataSource dataSource);

	default void removeDataSource(@Nonnull UUID id)
	{
		DataSource dataSource = findDataSource(id);
		if(dataSource == null)
		{
			throw new IllegalArgumentException("Didn't found datasource with id: " + id);
		}

		removeDataSource(dataSource);
	}

	void removeDataSource(@Nonnull DataSource dataSource);

	void commit();

	void dispose();
}
