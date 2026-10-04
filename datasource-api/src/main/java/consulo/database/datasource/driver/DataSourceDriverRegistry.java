/*
 * Copyright 2013-2026 consulo.io
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

package consulo.database.datasource.driver;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.application.Application;
import consulo.application.progress.ProgressIndicator;
import consulo.database.datasource.model.DataSource;
import jakarta.annotation.Nonnull;

import java.io.IOException;

/**
 * Installs and verifies the predefined drivers of the data source providers, shared by every provider. The installed drivers are
 * scanned on the first request, not when the application starts.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@ServiceAPI(ComponentScope.APPLICATION)
public interface DataSourceDriverRegistry {
    @Nonnull
    static DataSourceDriverRegistry getInstance() {
        return Application.get().getInstance(DataSourceDriverRegistry.class);
    }

    /**
     * Resolves the driver a data source runs on:
     * <ul>
     * <li>the driver selected in {@link consulo.database.datasource.configurable.GenericPropertyKeys#DRIVER} when its provider lists
     * it, otherwise the preferred driver - the first of {@link consulo.database.datasource.provider.DataSourceProvider#getDriverIds()};</li>
     * <li>the version selected in {@link consulo.database.datasource.configurable.GenericPropertyKeys#DRIVER_VERSION} when the
     * description of that driver lists it or it is installed, otherwise the latest version - the first one the description lists.</li>
     * </ul>
     * A version which is not installed is downloaded; an installed one is reused, verified once per run, and a missing or damaged file
     * is downloaded again.
     *
     * @return the driver with its start option and its verified files
     * @throws IOException           when a download fails or a downloaded file does not match its checksum
     * @throws IllegalStateException when the provider lists no driver
     */
    @Nonnull
    DataSourceDriver resolve(@Nonnull ProgressIndicator indicator, @Nonnull DataSource dataSource) throws IOException;
}
