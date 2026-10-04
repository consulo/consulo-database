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

package consulo.database.datasource.transport.ui;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.disposer.Disposable;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Builds the views of the data of a data source: of the result of a query, and of the rows of a child of a database - a table,
 * a collection.
 *
 * @author VISTALL
 * @since 21/10/2021
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface DataSourceTransportResultPresentation<R>
{
	ExtensionPointName<DataSourceTransportResultPresentation> EP_NAME = ExtensionPointName.create(DataSourceTransportResultPresentation.class);

	boolean accept(@Nonnull DataSource dataSource);

	/**
	 * Splits a result into the parts which are shown in views of their own, each built by {@link #buildComponentForResult} - for
	 * example one part per result set of a script. A result is a single part by default.
	 */
	default List<R> splitResult(R result)
	{
		return List.of(result);
	}

	/**
	 * The view of a result which was fetched or executed already.
	 *
	 * @param dbName  the database of the child the result belongs to, null for the result of a query
	 * @param childId the child the result belongs to, null for the result of a query
	 * @param parent  disposes the view
	 */
	@RequiredUIAccess
	Component buildComponentForResult(@Nonnull R result,
									  @Nonnull Project project,
									  DataSource dataSource,
									  @Nullable String dbName,
									  @Nullable String childId,
									  Disposable parent);

	/**
	 * The view of a child of a database - a table, a collection - which loads the rows by itself: page by page, and again on reload.
	 *
	 * @param parent disposes the view, and stops a load which is still running
	 * @return null when there is no such view - the rows are then fetched by {@link DataSourceTransport#fetchData} and shown by
	 * {@link #buildComponentForResult}
	 */
	@RequiredUIAccess
	default @Nullable Component buildComponentForChild(Project project,
													   DataSource dataSource,
													   String dbName,
													   String childId,
													   Disposable parent)
	{
		return null;
	}
}
