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

import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.configurable.PropertiesHolderImpl;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceImpl implements DataSource
{
	public static final String CONTAINER_NAME = "main";

	protected UUID myId;
	protected String myName;
	protected DataSourceProvider myProvider;
	protected boolean myApplicationAware;
	protected PropertiesHolderImpl myPropertiesHolder;

	public DataSourceImpl(UUID id, String name, DataSourceProvider provider, boolean applicationAware)
	{
		myId = id;
		myName = name;
		myProvider = provider;
		myApplicationAware = applicationAware;
		myPropertiesHolder = new PropertiesHolderImpl(CONTAINER_NAME);
	}

	protected DataSourceImpl()
	{
		myPropertiesHolder = new PropertiesHolderImpl(CONTAINER_NAME);
	}

	public void copyFrom(EditableDataSourceImpl dataSource)
	{
		myName = dataSource.getName();
		myProvider = dataSource.getProvider();
		myApplicationAware = dataSource.isApplicationAware();
		myPropertiesHolder.copyFrom(dataSource.myPropertiesHolder);
	}

	@Nonnull
	@Override
	public DataSourceProvider getProvider()
	{
		return myProvider;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return myName;
	}

	@Nonnull
	@Override
	public UUID getId()
	{
		if(myId == null)
		{
			myId = UUID.randomUUID();
		}
		return myId;
	}

	@Nonnull
	@Override
	public PropertiesHolder getProperties()
	{
		return myPropertiesHolder;
	}

	@Override
	public boolean isApplicationAware()
	{
		return myApplicationAware;
	}

	@Override
	public String toString()
	{
		return myId.toString();
	}
}
