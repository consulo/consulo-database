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
package consulo.database.sqlite;

import consulo.configurable.SimpleConfigurableByProperties;
import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.model.EditableDataSource;
import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.ui.Component;
import consulo.ui.Space;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.TabbedLayout;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nonnull;

/**
 * The database file of a SQLite data source
 *
 * @author VISTALL
 * @since 2026-10-31
 */
public class SqliteConfigurable extends SimpleConfigurableByProperties {
    private final EditableDataSource myDataSource;

    public SqliteConfigurable(EditableDataSource dataSource) {
        myDataSource = dataSource;
    }

    @RequiredUIAccess
    @Nonnull
    @Override
    protected Component createLayout(PropertyBuilder propertyBuilder, @Nonnull Disposable uiDisposable) {
        EditablePropertiesHolder propertiesHolder = myDataSource.getProperties();

        FileChooserTextBoxBuilder.Controller fileBox = FileChooserTextBoxBuilder.create(null)
            .uiDisposable(uiDisposable)
            .dialogTitle(LocalizeValue.localizeTODO("Database File"))
            .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())
            .build();

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(LocalizeValue.localizeTODO("File"), fileBox.getComponent());
        propertyBuilder.add(fileBox.getComponent(),
            () -> propertiesHolder.get(GenericPropertyKeys.DATABASE_NAME),
            it -> propertiesHolder.set(GenericPropertyKeys.DATABASE_NAME, it));

        Component component = builder.build();
        component.paddingBuilder().allSet(Space.MEDIUM).apply();

        TabbedLayout tabs = TabbedLayout.create();
        tabs.addTab("Connection", component);
        return tabs;
    }
}
