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

package consulo.database.impl.store;

import consulo.component.persist.PersistentStateComponent;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.configurable.PropertiesHolderImpl;
import consulo.database.impl.model.DataSourceImpl;
import consulo.database.impl.model.UnknownDataSourceProvider;
import consulo.disposer.Disposable;
import consulo.proxy.EventDispatcher;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-10-26
 */
public abstract class DataManagerStoreImpl implements PersistentStateComponent<Element>
{
	private final List<DataSourceImpl> myDataSources = new ArrayList<>();

	private final EventDispatcher<DataSourceStoreChangeListener> myDispatcher = EventDispatcher.create(DataSourceStoreChangeListener.class);

	public void addListener(DataSourceStoreChangeListener listener, Disposable disposable)
	{
		myDispatcher.addListener(listener, disposable);
	}

	@Nonnull
	public List<DataSourceImpl> getDataSources()
	{
		return myDataSources;
	}

	public void setDataSources(@Nonnull List<DataSourceImpl> dataSources)
	{
		myDataSources.clear();
		myDataSources.addAll(dataSources);
	}

	@Nullable
	@Override
	public Element getState()
	{
		Element state = new Element("state");
		for(DataSource source : myDataSources)
		{
			Element dataSourceElement = new Element("datasource");
			dataSourceElement.setAttribute("id", source.getId().toString());
			dataSourceElement.setAttribute("name", source.getName());
			dataSourceElement.setAttribute("provider", source.getProvider().getId());

			PropertiesHolderImpl h = (PropertiesHolderImpl) source.getProperties();

			dataSourceElement.addContent(h.toXmlState());

			state.addContent(dataSourceElement);
		}
		return state;
	}

	@Override
	public void loadState(Element state)
	{
		List<DataSourceImpl> dataSources = new ArrayList<>();

		for(Element element : state.getChildren())
		{
			String id = element.getAttributeValue("id");
			String name = element.getAttributeValue("name");
			String provider = element.getAttributeValue("provider");
			if(id == null || name == null || provider == null)
			{
				continue;
			}

			DataSourceProvider dataSourceProvider = findProvider(provider);

			DataSourceImpl dataSource = new DataSourceImpl(UUID.fromString(id), name, dataSourceProvider, isApplicationAware());

			Element propertiesElement = element.getChild(PropertiesHolderImpl.TAG_NAME);
			if(propertiesElement != null)
			{
				PropertiesHolderImpl h = (PropertiesHolderImpl) dataSource.getProperties();

				h.fromXmlState(propertiesElement);
			}

			dataSources.add(dataSource);
		}

		setDataSources(dataSources);

		myDispatcher.getMulticaster().storeChanged();
	}

	protected boolean isApplicationAware()
	{
		return false;
	}

	@Nonnull
	private static DataSourceProvider findProvider(String id)
	{
		for(DataSourceProvider provider : DataSourceProvider.EP_NAME.getExtensionList())
		{
			if(id.equals(provider.getId()))
			{
				return provider;
			}
		}

		return new UnknownDataSourceProvider(id);
	}
}
