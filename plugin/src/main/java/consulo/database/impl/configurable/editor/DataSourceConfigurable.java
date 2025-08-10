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

package consulo.database.impl.configurable.editor;

import consulo.component.ProcessCanceledException;
import consulo.configurable.ConfigurationException;
import consulo.configurable.NamedConfigurable;
import consulo.configurable.UnnamedConfigurable;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.provider.DataSourceConfigurationException;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.impl.localize.DatabaseLocalize;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.*;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.border.BorderPosition;
import consulo.ui.border.BorderStyle;
import consulo.ui.image.Image;
import consulo.ui.layout.DockLayout;
import consulo.util.concurrent.AsyncResult;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceConfigurable extends NamedConfigurable<EditableDataSource>
{
	private final Project myProject;

	private final EditableDataSource myDataSource;
	private final Runnable myTreeUpdater;

	private UnnamedConfigurable myInnerConfigurable;

	private CheckBox myApplicationAwareBox;

	@RequiredUIAccess
	public DataSourceConfigurable(Project project, EditableDataSource dataSource, Runnable treeUpdater)
	{
		super(true, treeUpdater);
		myProject = project;
		myDataSource = dataSource;
		myTreeUpdater = treeUpdater;
	}

	@RequiredUIAccess
	@Override
	public boolean isModified()
	{
		return myInnerConfigurable != null && myInnerConfigurable.isModified();
	}

	@RequiredUIAccess
	@Override
	public void apply() throws ConfigurationException
	{
		if(myInnerConfigurable != null)
		{
			myInnerConfigurable.apply();
		}
	}

	@RequiredUIAccess
	@Override
	public void reset()
	{
		updateName();

		myApplicationAwareBox.setValue(myDataSource.isApplicationAware());

		if(myInnerConfigurable != null)
		{
			myInnerConfigurable.reset();
		}
	}

	@RequiredUIAccess
	@Nullable
	@Override
	protected Component createTopRightComponent(TextBox nameField, Disposable parentUIDisposable)
	{
		CheckBox applicationAwareBox = CheckBox.create(DatabaseLocalize.labelApplicationAwareText());
		applicationAwareBox.addValueListener(event ->
		{
			myDataSource.setApplicationAware(event.getValue());

			myTreeUpdater.run();
		});

		return myApplicationAwareBox = applicationAwareBox;
	}

	@RequiredUIAccess
	@Override
	public void disposeUIResources()
	{
		super.disposeUIResources();

		myInnerConfigurable = null;
	}

	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.ofNullable(myDataSource.getName());
	}

	@Override
	public void setDisplayName(String name)
	{
		myDataSource.setName(name);
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return null;
	}

	@Override
	public EditableDataSource getEditableObject()
	{
		return null;
	}

	@Override
	public String getBannerSlogan()
	{
		return null;
	}

	@Nonnull
	@Override
	@RequiredUIAccess
	public Component createOptionsPanel(Disposable parentUIDisposable)
	{
		if(myInnerConfigurable == null)
		{
			myInnerConfigurable = myDataSource.getProvider().createConfigurable(myDataSource);
		}

		DockLayout panel = DockLayout.create();
		panel.center(myInnerConfigurable.createUIComponent(parentUIDisposable));

		Button testButton = Button.create(LocalizeValue.localizeTODO("Test Connection"), (event) ->
		{
			try
			{
				apply();
			}
			catch(ConfigurationException ignored)
			{
				return;
			}

			try
			{
				myDataSource.getProvider().validateConfiguration(myDataSource.getProperties());
			}
			catch(DataSourceConfigurationException e)
			{
				Alerts.okError(e.getMessageValue()).showAsync();
				return;
			}

			DataSourceTransportManager dataSourceTransportManager = DataSourceTransportManager.getInstance(myProject);

			AsyncResult<Void> result = dataSourceTransportManager.testConnection(myDataSource);

			UIAccess uiAccess = UIAccess.current();

			result.doWhenDone(() -> uiAccess.give(() -> Alerts.okInfo("Connection success").showAsync()));

			result.doWhenRejectedWithThrowable(throwable -> {
				if(throwable instanceof ProcessCanceledException)
				{
					// canceled no need info
					return;
				}
				uiAccess.give(() -> Alerts.okError("Connection failed: " + throwable.getMessage()).showAsync());
			});
		});

		DockLayout buttonPanel = DockLayout.create().right(testButton);
		buttonPanel.addBorder(BorderPosition.RIGHT, BorderStyle.EMPTY, null, 10);

		panel.bottom(buttonPanel);

		return panel;
	}
}
