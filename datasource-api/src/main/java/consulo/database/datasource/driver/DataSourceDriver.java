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

import jakarta.annotation.Nonnull;

import java.nio.file.Path;
import java.util.List;

/**
 * A driver resolved for a data source: installed, its files checked against their SHA-256.
 *
 * @param id      the driver id, one of {@link consulo.database.datasource.provider.DataSourceProvider#getDriverIds()}
 * @param name    the name of the driver, for display
 * @param version the installed version of the driver
 * @param start   how the driver is started
 * @param files   the files of the driver in the order of its description; unmodifiable
 * @author VISTALL
 * @since 2026-10-04
 */
public record DataSourceDriver(@Nonnull String id,
                               @Nonnull String name,
                               @Nonnull String version,
                               @Nonnull DataSourceDriverStart start,
                               @Nonnull List<Path> files) {
    public DataSourceDriver {
        files = List.copyOf(files);
    }
}
