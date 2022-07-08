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
import consulo.disposer.Disposable;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 21/10/2021
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface DataSourceTransportResultPresentation<R>
{
	ExtensionPointName<DataSourceTransportResultPresentation> EP_NAME = ExtensionPointName.create(DataSourceTransportResultPresentation.class);

	boolean accept(@Nonnull DataSource dataSource);

	JComponent buildComponentForResult(@Nonnull R result, @Nonnull Project project, DataSource dataSource, String dbName, String childId, Disposable parent);
}
