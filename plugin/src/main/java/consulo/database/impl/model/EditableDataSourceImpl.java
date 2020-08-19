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

package consulo.database.impl.model;

import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.configurable.EditablePropertiesHolderImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public class EditableDataSourceImpl extends DataSourceImpl implements EditableDataSource
{
	private DataSourceImpl myOriginal;

	public EditableDataSourceImpl(DataSourceImpl original)
	{
		super(original.myModel);

		myOriginal = original;

		myId = original.getId();
		myName = original.getName();
		myProvider = original.getProvider();

		myPropertiesHolder = new EditablePropertiesHolderImpl(CONTAINER_NAME);
		myPropertiesHolder.copyFrom(original.myPropertiesHolder);
	}

	public EditableDataSourceImpl(DataSourceModel manager, String name, DataSourceProvider provider)
	{
		super(manager);

		myName = name;
		myProvider = provider;
		myPropertiesHolder = new EditablePropertiesHolderImpl(CONTAINER_NAME);
	}

	@Nonnull
	@Override
	public EditablePropertiesHolder getProperties()
	{
		return (EditablePropertiesHolder) super.getProperties();
	}

	@Override
	public void setName(@Nonnull String name)
	{
		myName = name;
	}

	@Nullable
	public DataSourceImpl getOriginal()
	{
		return myOriginal;
	}
}
