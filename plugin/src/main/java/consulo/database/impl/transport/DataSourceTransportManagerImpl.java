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

package consulo.database.impl.transport;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ServiceImpl;
import consulo.application.progress.PerformInBackgroundOption;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceEvent;
import consulo.database.datasource.model.DataSourceListener;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.datasource.transport.DataSourceTransportListener;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.project.Project;
import consulo.util.concurrent.AsyncResult;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdom.Element;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
@Singleton
@State(name = "DataSourceTransportManagerImpl", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
@ServiceImpl
public class DataSourceTransportManagerImpl implements DataSourceTransportManager, PersistentStateComponent<Element>
{
	private final Project myProject;

	private final DataSourceManager myDataSourceManager;

	private final Map<UUID, DataSourceState> myStates = new ConcurrentHashMap<>();

	@Inject
	public DataSourceTransportManagerImpl(Project project, DataSourceManager dataSourceManager)
	{
		myProject = project;
		myDataSourceManager = dataSourceManager;

		myProject.getMessageBus().connect().subscribe(DataSourceListener.class, new DataSourceListener()
		{
			@Override
			public void dataSourceEvent(DataSourceEvent event)
			{
				if(event.getAction() == DataSourceEvent.Action.REMOVE)
				{
					myStates.remove(event.getDataSource().getId());
				}
			}
		});
	}

	@Nonnull
	@Override
	public AsyncResult<Void> testConnection(@Nonnull DataSource dataSource)
	{
		DataSourceTransport dataSourceTransport = findTransport(dataSource);

		AsyncResult<Void> result = AsyncResult.undefined();

		final DataSourceTransport finalDataSourceTransport = dataSourceTransport;
		new Task.ConditionalModal(myProject, "Testing connection", true, PerformInBackgroundOption.DEAF)
		{
			@Override
			public void run(@Nonnull ProgressIndicator indicator)
			{
				finalDataSourceTransport.testConnection(indicator, (Project) myProject, dataSource, result);

				result.waitFor(-1);
			}
		}.queue();
		return result;
	}

	@RequiredReadAction
	@Override
	@SuppressWarnings("unchecked")
	public void refreshAll()
	{
		List<? extends DataSource> dataSources = myDataSourceManager.getDataSources();

		DataSourceTransportListener publisher = myProject.getMessageBus().syncPublisher(DataSourceTransportListener.class);

		Task.Backgroundable.queue(myProject, "Refreshing data sources...", true, indicator ->
		{
			for(DataSource dataSource : dataSources)
			{
				DataSourceTransport transport = findTransport(dataSource);

				AsyncResult<PersistentStateComponent<?>> result = AsyncResult.undefined();

				transport.loadInitialData(indicator, myProject, dataSource, result);

				result.doWhenDone(state -> {
					myStates.put(dataSource.getId(), new DataSourceState(transport.getStateVersion(), null, state));

					publisher.dataUpdated(dataSource, state);
				});
			}
		});
	}

	@Nonnull
	private DataSourceTransport findTransport(DataSource dataSource)
	{
		DataSourceTransport dataSourceTransport = null;
		for(DataSourceTransport transport : DataSourceTransport.EP_NAME.getExtensionList())
		{
			if(transport.accept(dataSource))
			{
				dataSourceTransport = transport;
				break;
			}
		}

		if(dataSourceTransport == null)
		{
			throw new UnsupportedOperationException("No fake transport. Broken distribution");
		}

		return dataSourceTransport;
	}

	@Override
	public <T extends PersistentStateComponent<?>> T getDataState(@Nonnull DataSource dataSource)
	{
		DataSourceState dataSourceState = myStates.get(dataSource.getId());
		if(dataSourceState == null)
		{
			return null;
		}

		DataSourceTransport transport = findTransport(dataSource);

		if(dataSourceState.getVersion() != transport.getStateVersion())
		{
			myStates.remove(dataSource.getId());
			return null;
		}

		return dataSourceState.getObjectState(dataSource, transport);
	}

	@Nonnull
	@Override
	public AsyncResult<Object> runQuery(@Nonnull DataSource dataSource, @Nonnull String query)
	{
		DataSourceTransport transport = findTransport(dataSource);

		AsyncResult<Object> result = AsyncResult.undefined();

		Task.Backgroundable.queue(myProject, "Executing query...", true, indicator ->
		{
			transport.runQuery(indicator, myProject, dataSource, query, result);
		});

		return result;
	}

	@Nullable
	@Override
	public Element getState()
	{
		Element rootElement = new Element("state");
		for(Map.Entry<UUID, DataSourceState> entry : myStates.entrySet())
		{
			Element stateElement = new Element("datasource");

			rootElement.addContent(stateElement);

			stateElement.setAttribute("id", entry.getKey().toString());

			Element dataSourceState = new Element("data-state");
			dataSourceState.setAttribute("version", String.valueOf(entry.getValue().getVersion()));
			dataSourceState.addContent(entry.getValue().toXmlState());

			stateElement.addContent(dataSourceState);
		}
		return rootElement;
	}

	@Override
	public void loadState(Element state)
	{
		for(Element element : state.getChildren())
		{
			String id = element.getAttributeValue("id");
			if(id == null)
			{
				continue;
			}

			UUID uuid = UUID.fromString(id);

			Element stateElement = element.getChild("data-state");

			if(stateElement.getContentSize() == 0)
			{
				continue;
			}

			int version = Integer.parseInt(stateElement.getAttributeValue("version", "0"));
		
			Element firstChild = stateElement.getChildren().get(0);

			myStates.put(uuid, new DataSourceState(version, firstChild, null));
		}
	}
}
