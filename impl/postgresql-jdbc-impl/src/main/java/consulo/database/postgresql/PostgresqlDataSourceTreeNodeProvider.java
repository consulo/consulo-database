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

package consulo.database.postgresql;

import consulo.annotation.component.ExtensionImpl;
import consulo.database.datasource.jdbc.provider.impl.JdbcDatabaseState;
import consulo.database.datasource.jdbc.ui.JdbcDataSourceTreeNodeProvider;
import consulo.database.datasource.jdbc.ui.tree.DatabaseJdbcDatabaseNode;
import consulo.database.datasource.model.DataSource;
import consulo.database.postgresql.node.PostgresqlDatabaseNode;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-30
 */
@ExtensionImpl(order = "before jdbc")
public class PostgresqlDataSourceTreeNodeProvider extends JdbcDataSourceTreeNodeProvider
{
	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return dataSource.getProvider() instanceof PostgresqlJdbcDataSourceProvider;
	}

	@Nonnull
	@Override
	protected DatabaseJdbcDatabaseNode createDatabaseNode(Project project, DataSource dataSource, JdbcDatabaseState state)
	{
		return new PostgresqlDatabaseNode(project, dataSource, state);
	}
}
