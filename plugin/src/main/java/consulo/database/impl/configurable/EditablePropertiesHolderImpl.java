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

package consulo.database.impl.configurable;

import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.configurable.GenericPropertyKey;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class EditablePropertiesHolderImpl extends PropertiesHolderImpl implements EditablePropertiesHolder
{
	private final Supplier<UUID> myOwnerId;

	/**
	 * @param ownerId the id of the data source which owns the properties - a secure value is kept in the password safe under a key of
	 *                that data source, so data sources never share a password
	 */
	public EditablePropertiesHolderImpl(@Nonnull String name, @Nonnull Supplier<UUID> ownerId)
	{
		super(name);
		myOwnerId = ownerId;
	}

	@Override
	public <T> void set(@Nonnull GenericPropertyKey<T> key, @Nullable T value)
	{
		if(value == null)
		{
			myValues.remove(key.toString());
		}
		else
		{
			myValues.put(key.toString(), new UnstableValue(key + "@" + myOwnerId.get(), value));
		}
	}
}
