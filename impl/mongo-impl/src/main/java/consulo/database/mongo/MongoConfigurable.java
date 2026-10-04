/*
 * Copyright 2013-2026 consulo.io
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


package consulo.database.mongo;

import consulo.configurable.ConfigurationException;
import consulo.configurable.SimpleConfigurableByProperties;
import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.SecureStringProperty;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.mongo.localize.MongoLocalize;
import consulo.database.mongo.transport.MongoConnectionSettings;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.ui.Component;
import consulo.ui.IntBox;
import consulo.ui.Label;
import consulo.ui.PasswordBox;
import consulo.ui.Space;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.style.StandardColors;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

/**
 * Settings of a MongoDB data source. The password is kept in the password safe; the connection string is stored as plain text, so
 * it must not carry one - {@link #apply} refuses to store such a string.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public class MongoConfigurable extends SimpleConfigurableByProperties {
    private final EditableDataSource myDataSource;

    private @Nullable TextBox myConnectionStringBox;

    public MongoConfigurable(EditableDataSource dataSource) {
        myDataSource = dataSource;
    }

    @RequiredUIAccess
    @Override
    protected Component createLayout(PropertyBuilder propertyBuilder, Disposable uiDisposable) {
        EditablePropertiesHolder properties = myDataSource.getProperties();

        FormBuilder builder = FormBuilder.create();

        TextBox hostBox = TextBox.create();
        builder.addLabeled(MongoLocalize.settingsHostLabel(), hostBox);
        propertyBuilder.add(hostBox, () -> properties.get(GenericPropertyKeys.HOST), it -> properties.set(GenericPropertyKeys.HOST, it));

        IntBox portBox = IntBox.create();
        portBox.setRange(0, 65535);
        builder.addLabeled(MongoLocalize.settingsPortLabel(), portBox);
        propertyBuilder.add(portBox, () -> properties.get(GenericPropertyKeys.PORT), it -> properties.set(GenericPropertyKeys.PORT, it));

        TextBox loginBox = TextBox.create();
        builder.addLabeled(MongoLocalize.settingsLoginLabel(), loginBox);
        propertyBuilder.add(loginBox, () -> properties.get(GenericPropertyKeys.LOGIN), it -> properties.set(GenericPropertyKeys.LOGIN, it));

        PasswordBox passwordBox = PasswordBox.create();
        builder.addLabeled(MongoLocalize.settingsPasswordLabel(), passwordBox);
        SecureStringProperty.bind(propertyBuilder, passwordBox, properties, GenericPropertyKeys.PASSWORD, myDataSource);

        TextBox authSourceBox = TextBox.create();
        authSourceBox.setPlaceholder(LocalizeValue.of(MongoPropertyKeys.DEFAULT_AUTH_SOURCE));
        builder.addLabeled(MongoLocalize.settingsAuthSourceLabel(), authSourceBox);
        propertyBuilder.add(authSourceBox,
            () -> properties.get(MongoPropertyKeys.AUTH_SOURCE),
            it -> properties.set(MongoPropertyKeys.AUTH_SOURCE, it));

        TextBox databaseBox = TextBox.create();
        builder.addLabeled(MongoLocalize.settingsDatabaseLabel(), databaseBox);
        propertyBuilder.add(databaseBox,
            () -> properties.get(GenericPropertyKeys.DATABASE_NAME),
            it -> properties.set(GenericPropertyKeys.DATABASE_NAME, it));

        TextBox connectionStringBox = TextBox.create();
        myConnectionStringBox = connectionStringBox;
        connectionStringBox.setPlaceholder(MongoLocalize.settingsConnectionStringPlaceholder());
        // a long comment would make the form too wide for the dialog: the details go to the tooltip
        connectionStringBox.setToolTipText(MongoLocalize.settingsConnectionStringTooltip());
        builder.addLabeled(MongoLocalize.settingsConnectionStringLabel(), connectionStringBox);
        propertyBuilder.add(connectionStringBox,
            () -> properties.get(MongoPropertyKeys.CONNECTION_STRING),
            it -> properties.set(MongoPropertyKeys.CONNECTION_STRING, it));

        Label connectionStringComment = Label.create(MongoLocalize.settingsConnectionStringComment());
        connectionStringComment.setForegroundColor(StandardColors.GRAY);

        VerticalLayout layout = VerticalLayout.create();
        layout.add(builder.build());
        layout.add(connectionStringComment);
        layout.paddingBuilder().allSet(Space.MEDIUM).apply();
        return layout;
    }

    /**
     * Validates the connection string before anything is stored: Test Connection validates the applied properties, but OK applies
     * without it - and a password in the connection string must never reach the plain text storage.
     */
    @RequiredUIAccess
    @Override
    protected void apply(LayoutWrapper component) throws ConfigurationException {
        TextBox connectionStringBox = myConnectionStringBox;
        if (connectionStringBox != null) {
            LocalizeValue error = MongoConnectionSettings.checkConnectionString(connectionStringBox.getValue());
            if (error != null) {
                throw new ConfigurationException(error);
            }
        }

        super.apply(component);
    }
}
