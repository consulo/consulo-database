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
package consulo.database.sqlite;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.UnnamedConfigurable;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.provider.DataSourceConfigurationException;
import consulo.database.icon.DatabaseIconGroup;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * A SQLite database: a file, its path is the {@link GenericPropertyKeys#DATABASE_NAME}; there is no host, port or login.
 *
 * @author VISTALL
 * @since 2026-10-31
 */
@ExtensionImpl
public class SqliteJdbcDataSourceProvider extends JdbcDataSourceProvider {
    @Nonnull
    @Override
    public String getId() {
        return "sqlite";
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return LocalizeValue.localizeTODO("SQLite");
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return DatabaseIconGroup.providersSqlite();
    }

    @Nonnull
    @Override
    public UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource) {
        return new SqliteConfigurable((EditableDataSource) dataSource);
    }

    @Override
    public void validateConfiguration(@Nonnull PropertiesHolder propertiesHolder) throws DataSourceConfigurationException {
        if (StringUtil.isEmptyOrSpaces(propertiesHolder.get(GenericPropertyKeys.DATABASE_NAME))) {
            throw new DataSourceConfigurationException(LocalizeValue.localizeTODO("Database file is not set"));
        }
    }

    @Override
    public String buildJdbcUrl(DataSource dataSource) {
        String file = dataSource.getProperties().get(GenericPropertyKeys.DATABASE_NAME);
        return "jdbc:sqlite:" + StringUtil.notNullize(file);
    }

    @Nonnull
    @Override
    public List<String> getDriverIds() {
        return List.of("sqlite-xerial");
    }
}
