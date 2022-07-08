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

import consulo.application.progress.Task;
import consulo.dataContext.DataManager;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.datasource.transport.DataSourceTransportResult;
import consulo.database.datasource.transport.ui.DataSourceTransportResultPresentation;
import consulo.database.impl.editor.actions.NextPageAction;
import consulo.database.impl.editor.actions.PageAction;
import consulo.database.impl.editor.actions.PrevPageAction;
import consulo.database.impl.editor.actions.RefreshDataAction;
import consulo.disposer.Disposable;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorLocation;
import consulo.fileEditor.FileEditorState;
import consulo.fileEditor.FileEditorStateLevel;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.JBColor;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.action.AnSeparator;
import consulo.ui.ex.awt.JBLabel;
import consulo.ui.ex.awt.JBUI;
import consulo.ui.ex.awt.LoadingDecorator;
import consulo.util.concurrent.AsyncResult;
import consulo.util.dataholder.UserDataHolderBase;
import kava.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
	private DataSourceTransportResult myLastTransportResult;

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
		builder.add(new PrevPageAction());
		builder.add(new PageAction());
		builder.add(new NextPageAction());
		builder.add(new AnSeparator());
		builder.add(new RefreshDataAction());

		myLoadingDecorator = new LoadingDecorator(myTargetPanel, this, 0);

		ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DataSourceEditor", builder.build(), true);
		toolbar.getComponent().setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 1));
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

				AsyncResult<DataSourceTransportResult> result = AsyncResult.undefined();

				transport.fetchData(indicator, myProject, myDataSource, myFile.getDatabaseName(), myFile.getChildId(), result);

				result.doWhenDone(o -> {
					myLoadingDecorator.stopLoading();
					myLoading.set(false);

					myLastTransportResult = o;
					uiAccess.give(() -> {
						if(myLastResult != null)
						{
							myTargetPanel.remove(myLastResult);
						}

						JComponent newComponent = buildUI(o, myProject, myDataSource, myFile.getDatabaseName(), myFile.getChildId(), DataSourceFileEditor.this);
						myTargetPanel.add(myLastResult = newComponent, BorderLayout.CENTER);
					});
				});
			});
		}
	}

	@SuppressWarnings("unchecked")
	public static JComponent buildUI(@Nonnull Object result, @Nonnull Project project, DataSource dataSource, String dbName, String childId, Disposable parent)
	{
		DataSourceTransportResultPresentation target = null;
		for(DataSourceTransportResultPresentation presentation : DataSourceTransportResultPresentation.EP_NAME.getExtensionList())
		{
			if(presentation.accept(dataSource))
			{
				target = presentation;
				break;
			}
		}

		if(target == null)
		{
			return new JBLabel("Not supported result");
		}

		return target.buildComponentForResult(result, project, dataSource, dbName, childId, parent);
	}

	public long getRowsCount()
	{
		return myLastTransportResult == null ? 0 : myLastTransportResult.getRowsCount();
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
