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

package consulo.database.datasource.provider;

import consulo.localize.LocalizeValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 31/01/2021
 */
public class DataSourceConfigurationException extends Exception
{
	@Nonnull
	private final LocalizeValue myMessageValue;

	public DataSourceConfigurationException(@Nonnull LocalizeValue messageValue)
	{
		this(messageValue, null);
	}

	public DataSourceConfigurationException(@Nonnull LocalizeValue messageValue, @Nullable Exception parent)
	{
		super(messageValue.get(), parent);
		myMessageValue = messageValue;
	}

	@Nonnull
	public LocalizeValue getMessageValue()
	{
		return myMessageValue;
	}

	@Override
	@Nonnull
	public String getMessage()
	{
		return myMessageValue.get();
	}

	@Override
	public String getLocalizedMessage()
	{
		return getMessage();
	}
}
