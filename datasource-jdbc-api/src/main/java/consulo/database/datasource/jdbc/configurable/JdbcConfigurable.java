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

package consulo.database.datasource.jdbc.configurable;

import consulo.configurable.SimpleConfigurableByProperties;
import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.SecureString;
import consulo.database.datasource.model.EditableDataSource;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.ui.*;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.border.BorderStyle;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.TabbedLayout;
import consulo.ui.style.StandardColors;
import consulo.ui.util.FormBuilder;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class JdbcConfigurable extends SimpleConfigurableByProperties
{
	private static final Logger LOG = Logger.getInstance(JdbcConfigurable.class);

	private final EditableDataSource myDataSource;

	public JdbcConfigurable(EditableDataSource dataSource)
	{
		myDataSource = dataSource;
	}

	@RequiredUIAccess
	@Nonnull
	@Override
	protected Component createLayout(PropertyBuilder propertyBuilder, @Nonnull Disposable uiDisposable)
	{
		EditablePropertiesHolder propertiesHolder = myDataSource.getProperties();

		TabbedLayout tabs = TabbedLayout.create();

		FormBuilder builder = FormBuilder.create();

		TextBox hostBox = TextBox.create();
		builder.addLabeled(LocalizeValue.localizeTODO("Host"), hostBox);
		propertyBuilder.add(hostBox, () -> propertiesHolder.get(GenericPropertyKeys.HOST), it -> propertiesHolder.set(GenericPropertyKeys.HOST, it));

		IntBox portBox = IntBox.create();
		builder.addLabeled(LocalizeValue.localizeTODO("Port"), portBox);
		propertyBuilder.add(portBox, () -> propertiesHolder.get(GenericPropertyKeys.PORT), it -> propertiesHolder.set(GenericPropertyKeys.PORT, it));

		TextBox loginBox = TextBox.create();
		builder.addLabeled(LocalizeValue.localizeTODO("Login"), loginBox);
		propertyBuilder.add(loginBox, () -> propertiesHolder.get(GenericPropertyKeys.LOGIN), it -> propertiesHolder.set(GenericPropertyKeys.LOGIN, it));

		PasswordBox passwordBox = PasswordBox.create();
		builder.addLabeled(LocalizeValue.localizeTODO("Password"), passwordBox);
		propertyBuilder.add(passwordBox, () -> propertiesHolder.get(GenericPropertyKeys.PASSWORD).getValue(myDataSource), it ->
		{
			propertiesHolder.set(GenericPropertyKeys.PASSWORD, SecureString.raw(it));
		});

		TextBox databaseNameBox = TextBox.create();
		builder.addLabeled(LocalizeValue.localizeTODO("Database Name"), databaseNameBox);
		propertyBuilder.add(databaseNameBox, () -> propertiesHolder.get(GenericPropertyKeys.DATABASE_NAME), it -> propertiesHolder.set(GenericPropertyKeys.DATABASE_NAME, it));

		Component component = builder.build();
		component.addBorders(BorderStyle.EMPTY, null, 10);
		tabs.addTab("Connection", component);

		DockLayout properties = DockLayout.create();

		tabs.addTab("Properties", properties);
		return tabs;
	}
}
