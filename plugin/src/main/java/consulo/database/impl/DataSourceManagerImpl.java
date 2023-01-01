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

package consulo.database.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.*;
import consulo.database.impl.model.DataSourceImpl;
import consulo.database.impl.model.DataSourceModelImpl;
import consulo.database.impl.model.EditableDataSourceModelImpl;
import consulo.database.impl.store.ApplicationDataSourceStoreImpl;
import consulo.database.impl.store.ProjectDataSourceStoreImpl;
import consulo.disposer.Disposable;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
@Singleton
@State(name = "DataSourceManagerImpl", storages = @Storage("datasource.xml"))
@ServiceImpl
public class DataSourceManagerImpl implements DataSourceManager, Disposable
{
	private final Project myProject;

	private DataSourceModelImpl<DataSourceImpl> myModel = new DataSourceModelImpl<>();

	private EditableDataSourceModel myEditableDataSourceModel;

	private final ApplicationDataSourceStoreImpl myApplicationDataSourceStore;
	private final ProjectDataSourceStoreImpl myProjectDataSourceStore;

	@Inject
	public DataSourceManagerImpl(Project project, ApplicationDataSourceStoreImpl applicationDataSourceStore, ProjectDataSourceStoreImpl projectDataSourceStore)
	{
		myProject = project;
		myApplicationDataSourceStore = applicationDataSourceStore;
		myProjectDataSourceStore = projectDataSourceStore;

		// call it before listeners add
		dataStoreChanged();

		myApplicationDataSourceStore.addListener(this::dataStoreChanged, this);
		myProjectDataSourceStore.addListener(this::dataStoreChanged, this);
	}

	private void dataStoreChanged()
	{
		List<DataSourceImpl> dataSources = new ArrayList<>();
		dataSources.addAll(myApplicationDataSourceStore.getDataSources());
		dataSources.addAll(myProjectDataSourceStore.getDataSources());

		myModel.replaceAll(dataSources);
	}

	public void notifyListeners(@Nonnull List<DataSourceEvent> events)
	{
		for(DataSourceEvent event : events)
		{
			myProject.getMessageBus().syncPublisher(DataSourceListener.class).dataSourceEvent(event);
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DataSourceModel getModel()
	{
		myProject.getApplication().assertReadAccessAllowed();

		return myModel;
	}

	@Nonnull
	@Override
	public EditableDataSourceModel createEditableModel()
	{
		if(myEditableDataSourceModel != null)
		{
			throw new IllegalArgumentException("already created");
		}

		myEditableDataSourceModel = new EditableDataSourceModelImpl(this, myModel);
		return myEditableDataSourceModel;
	}

	@Override
	public void dispose()
	{
	}

	public void resetModelAndRefreshStore()
	{
		List<DataSourceImpl> applicationDataSources = new ArrayList<>();
		List<DataSourceImpl> projectDataSources = new ArrayList<>();

		for(DataSource dataSource : myModel.getDataSources())
		{
			if(dataSource.isApplicationAware())
			{
				applicationDataSources.add((DataSourceImpl) dataSource);
			}
			else
			{
				projectDataSources.add((DataSourceImpl) dataSource);
			}
		}

		myApplicationDataSourceStore.setDataSources(applicationDataSources);
		myProjectDataSourceStore.setDataSources(projectDataSources);

		myEditableDataSourceModel = null;
	}
}
