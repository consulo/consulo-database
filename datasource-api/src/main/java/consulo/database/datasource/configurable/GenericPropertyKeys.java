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

package consulo.database.datasource.configurable;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface GenericPropertyKeys
{
	GenericPropertyKey<String> HOST = GenericPropertyKey.create("host", String.class, "localhost");

	GenericPropertyKey<Integer> PORT = GenericPropertyKey.create("port", Integer.class, 0);

	GenericPropertyKey<String> LOGIN = GenericPropertyKey.create("login", String.class);

	GenericPropertyKey<SecureString> PASSWORD = GenericPropertyKey.create("password", SecureString.class, SecureString.EMPTY);

	GenericPropertyKey<String> DATABASE_NAME = GenericPropertyKey.create("database-name", String.class);
}
