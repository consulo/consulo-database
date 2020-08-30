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

package consulo.database.datasource.transport;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import consulo.database.datasource.model.DataSource;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface DataSourceTransport<STATE extends PersistentStateComponent<?>>
{
	ExtensionPointName<DataSourceTransport> EP_NAME = ExtensionPointName.create("consulo.database.transport");

	boolean accept(@Nonnull DataSource dataSource);

	void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result);

	void loadInitialData(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<STATE> result);

	default void fetchData(@Nonnull ProgressIndicator indicator,
						   @Nonnull Project project,
						   @Nonnull DataSource dataSource,
						   @Nonnull String databaseName,
						   @Nonnull String childId,
						   @Nonnull AsyncResult<Object> result)
	{
		result.setDone();
	}

	@RequiredUIAccess
	default void fetchDataEnded(@Nonnull ProgressIndicator indicator,
								@Nonnull Project project,
								@Nonnull DataSource dataSource,
								@Nonnull String dbName,
								@Nonnull String childId,
								@Nonnull Object data,
								@Nonnull Consumer<JComponent> setter)
	{
	}

	@Nonnull
	Class<STATE> getStateClass();
}
