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

package consulo.database.datasource.model;

import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.provider.DataSourceProvider;

import jakarta.annotation.Nonnull;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public interface DataSource
{
	@Nonnull
	DataSourceProvider getProvider();

	@Nonnull
	String getName();

	/**
	 * @return unique uuid for datasource. this id never change
	 */
	@Nonnull
	UUID getId();

	@Nonnull
	PropertiesHolder getProperties();

	boolean isApplicationAware();
}
