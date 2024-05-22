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

package consulo.database.datasource.configurable;

import consulo.database.datasource.model.DataSource;
import jakarta.annotation.Nonnull;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 05/04/2021
 */
public interface SecureString
{
	SecureString EMPTY = dataSource -> null;

	class RawSecureString implements SecureString
	{
		private final String myRawValue;

		public RawSecureString(String rawValue)
		{
			myRawValue = rawValue;
		}

		@Override
		public String getValue(@Nonnull DataSource dataSource)
		{
			return Objects.toString(myRawValue, "");
		}

		@Override
		public String toString()
		{
			return myRawValue;
		}

		public String getRawValue()
		{
			return myRawValue;
		}

		@Nonnull
		public String getStoreValue(String key)
		{
			return key;
		}
	}

	String getValue(@Nonnull DataSource dataSource);

	@Nonnull
	public static SecureString raw(@Nonnull String rawValue)
	{
		return new RawSecureString(rawValue);
	}
}
