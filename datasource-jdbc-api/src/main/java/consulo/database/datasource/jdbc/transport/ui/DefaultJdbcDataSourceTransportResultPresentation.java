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

package consulo.database.datasource.jdbc.transport.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.jdbc.transport.DefaultJdbcDataSourceTransport;
import consulo.database.datasource.jdbc.transport.JdbcQueryResultWrapper;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.ui.DataSourceTransportResultPresentation;
import consulo.disposer.Disposable;
import consulo.project.Project;

import jakarta.annotation.Nonnull;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 21/10/2021
 */
@ExtensionImpl(order = "last")
public class DefaultJdbcDataSourceTransportResultPresentation implements DataSourceTransportResultPresentation<JdbcQueryResultWrapper>
{
	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return dataSource.getProvider() instanceof JdbcDataSourceProvider;
	}

	@Override
	public JComponent buildComponentForResult(@Nonnull JdbcQueryResultWrapper result, @Nonnull Project project, DataSource dataSource, String dbName, String childId, Disposable parent)
	{
		return DefaultJdbcDataSourceTransport.buildResultUI(result.getJdbcQueryResult(), project, dataSource, dbName, childId, parent);
	}
}
