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

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 05/04/2021
 */
public class SecureString implements Supplier<String>
{
	private static final SecureString EMPTY = of("");

	@Nonnull
	public static SecureString of()
	{
		return EMPTY;
	}

	@Nonnull
	public static SecureString of(@Nonnull String value)
	{
		return new SecureString(value);
	}

	private final String myValue;

	private SecureString(@Nonnull String value)
	{
		myValue = value;
	}

	@Override
	public String get()
	{
		return myValue;
	}

	@Override
	public String toString()
	{
		return get();
	}
}
