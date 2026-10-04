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


package consulo.database.mongo;

import consulo.database.datasource.configurable.GenericPropertyKey;

/**
 * MongoDB specific data source properties. The generic ones - host, port, login, password and the default database - are
 * {@link consulo.database.datasource.configurable.GenericPropertyKeys}.
 * <p>
 * Only {@code String} and {@code Integer} keys are allowed: the properties holder reads nothing else back after a restart.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public interface MongoPropertyKeys {
    String DEFAULT_AUTH_SOURCE = "admin";

    int DEFAULT_PORT = 27017;

    /**
     * The database the credentials are defined in.
     */
    GenericPropertyKey<String> AUTH_SOURCE = GenericPropertyKey.create("auth-source", String.class, DEFAULT_AUTH_SOURCE);

    /**
     * An optional {@code mongodb://} or {@code mongodb+srv://} connection string, which replaces host and port. It never holds the
     * password: it is stored as plain text, so the password stays in the password field.
     */
    GenericPropertyKey<String> CONNECTION_STRING = GenericPropertyKey.create("connection-string", String.class);
}
