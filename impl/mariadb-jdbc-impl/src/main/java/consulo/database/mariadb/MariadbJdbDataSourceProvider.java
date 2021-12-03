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

package consulo.database.mariadb;

import com.intellij.openapi.options.UnnamedConfigurable;
import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.SecureString;
import consulo.database.datasource.jdbc.configurable.JdbcConfigurable;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.icon.DatabaseIconGroup;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class MariadbJdbDataSourceProvider extends JdbcDataSourceProvider
{
	@Nonnull
	@Override
	public String getId()
	{
		return "mariadb";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("MariaDB");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return DatabaseIconGroup.mariadb();
	}

	@Nonnull
	@Override
	public UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource)
	{
		return new JdbcConfigurable((EditableDataSource) dataSource);
	}

	@Override
	public void fillDefaultProperties(@Nonnull EditablePropertiesHolder propertiesHolder)
	{
		propertiesHolder.set(GenericPropertyKeys.PORT, 3306);
		propertiesHolder.set(GenericPropertyKeys.LOGIN, "root");
		propertiesHolder.set(GenericPropertyKeys.PASSWORD, SecureString.of());
	}

	@Override
	public void fillDrivers(Map<String, String> map)
	{
		map.put("mariadb-java-client-2.6.2.jar", "https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.6.2/mariadb-java-client-2.6.2.jar");
	}
}