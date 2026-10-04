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


package consulo.database.mongo;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.UnnamedConfigurable;
import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.configurable.GenericPropertyKey;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.json.JsonDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.provider.DataSourceConfigurationException;
import consulo.database.icon.DatabaseIconGroup;
import consulo.database.mongo.transport.MongoConnectionSettings;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
@ExtensionImpl
public class MongoDbDataSourceProvider implements JsonDataSourceProvider {
    @Override
    public String getId() {
        return "mongodb";
    }

    @Override
    public LocalizeValue getName() {
        return LocalizeValue.of("MongoDB");
    }

    @Override
    public Image getIcon() {
        return DatabaseIconGroup.providersMongodb();
    }

    @Override
    public UnnamedConfigurable createConfigurable(DataSource dataSource) {
        return new MongoConfigurable((EditableDataSource) dataSource);
    }

    @Override
    public void validateConfiguration(PropertiesHolder propertiesHolder) throws DataSourceConfigurationException {
        MongoConnectionSettings.validate(propertiesHolder);
    }

    @Override
    public void fillDefaultProperties(EditablePropertiesHolder propertiesHolder) {
        propertiesHolder.set(GenericPropertyKeys.PORT, MongoPropertyKeys.DEFAULT_PORT);
        propertiesHolder.set(MongoPropertyKeys.AUTH_SOURCE, MongoPropertyKeys.DEFAULT_AUTH_SOURCE);
    }

    /**
     * The description {@code mongo-java.json} starts {@code java-rt} with agent {@code mongo} and may list only 5.x versions: the runtime
     * module is compiled against the 5.8.1 driver ({@code datasource-mongo-rt/pom.xml}).
     */
    @Override
    public List<String> getDriverIds() {
        return List.of("mongo-java");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getDefaultValue(GenericPropertyKey<T> key) {
        if (key == GenericPropertyKeys.PORT) {
            return (T) Integer.valueOf(MongoPropertyKeys.DEFAULT_PORT);
        }
        if (key == MongoPropertyKeys.AUTH_SOURCE) {
            return (T) MongoPropertyKeys.DEFAULT_AUTH_SOURCE;
        }
        return null;
    }
}
