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

package consulo.database.impl.editor;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.impl.editor.actions.RefreshDataAction;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.concurrent.AsyncResult;
import consulo.util.dataholder.UserDataHolderBase;
import kava.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DataSourceFileEditor extends UserDataHolderBase implements FileEditor
{
	private final Project myProject;
	private final DataSourceVirtualFile myFile;
	private final LoadingDecorator myLoadingDecorator;
	private final AtomicBoolean myLoading = new AtomicBoolean();
	private final DataSource myDataSource;
	private final JPanel myTargetPanel;

	private JComponent myLastResult;

	@RequiredUIAccess
	public DataSourceFileEditor(Project project, DataSourceVirtualFile file)
	{
		myProject = project;
		myFile = file;
		myDataSource = myFile.getDataSource();
		myTargetPanel = new JPanel(new BorderLayout());
		DataManager.registerDataProvider(myTargetPanel, key ->
		{
			if(key == DataSourceFileEditorKeys.EDITOR)
			{
				return DataSourceFileEditor.this;
			}
			return null;
		});

		ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
		builder.add(new RefreshDataAction());

		myLoadingDecorator = new LoadingDecorator(myTargetPanel, this, 0);

		ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DataSourceEditor", builder.build(), true);
		toolbar.setTargetComponent(myTargetPanel);

		myTargetPanel.add(toolbar.getComponent(), BorderLayout.NORTH);

		UIAccess uiAccess = UIAccess.current();

		loadData(uiAccess);
	}

	public void loadData(@Nonnull UIAccess uiAccess)
	{
		if(myLoading.compareAndSet(false, true))
		{
			myLoadingDecorator.startLoading(false);
			Task.Backgroundable.queue(myProject, "Fetching data...", true, indicator ->
			{
				DataSourceTransport transport = null;
				for(DataSourceTransport dataSourceTransport : DataSourceTransport.EP_NAME.getExtensionList())
				{
					if(dataSourceTransport.accept(myDataSource))
					{
						transport = dataSourceTransport;
						break;
					}
				}

				assert transport != null;

				AsyncResult<Object> result = AsyncResult.undefined();

				transport.fetchData(indicator, myProject, myDataSource, myFile.getDatabaseName(), myFile.getChildId(), result);

				final DataSourceTransport finalTransport = transport;
				result.doWhenDone(o -> {
					myLoadingDecorator.stopLoading();
					myLoading.set(false);

					uiAccess.give(() -> {
						if(myLastResult != null)
						{
							myTargetPanel.remove(myLastResult);
						}
						Consumer setter = component -> myTargetPanel.add(myLastResult = (JComponent) component, BorderLayout.CENTER);
						finalTransport.fetchDataEnded(indicator, myProject, myDataSource, myFile.getDatabaseName(), myFile.getChildId(), o, DataSourceFileEditor.this, setter);
					});
				});
			});
		}
	}

	@Nonnull
	@Override
	public JComponent getComponent()
	{
		return myLoadingDecorator.getComponent();
	}

	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent()
	{
		return myLoadingDecorator.getComponent();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "datasource";
	}

	@Nonnull
	@Override
	public FileEditorState getState(@Nonnull FileEditorStateLevel fileEditorStateLevel)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void setState(@Nonnull FileEditorState fileEditorState)
	{

	}

	@Override
	public boolean isModified()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void selectNotify()
	{

	}

	@Override
	public void deselectNotify()
	{

	}

	@Override
	public void addPropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener)
	{

	}

	@Override
	public void removePropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener)
	{

	}

	@Nullable
	@Override
	public FileEditorLocation getCurrentLocation()
	{
		return null;
	}

	@Override
	public void dispose()
	{

	}
}
