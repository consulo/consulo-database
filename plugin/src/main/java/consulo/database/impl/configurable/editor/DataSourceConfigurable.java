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
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.driver.DataSourceDriverRegistry;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.provider.DataSourceConfigurationException;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.impl.driver.DataSourceDriverRegistryImpl;
import consulo.database.impl.driver.DriverJson;
import consulo.database.impl.driver.DriverVersionJson;
import consulo.database.impl.localize.DatabaseLocalize;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.*;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;
import consulo.util.concurrent.AsyncResult;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceConfigurable extends NamedConfigurable<EditableDataSource> {
    private final Project myProject;

    private final EditableDataSource myDataSource;
    private final Runnable myTreeUpdater;

    private UnnamedConfigurable myInnerConfigurable;

    private CheckBox myApplicationAwareBox;

    private List<DriverChoice> myDriverChoices = List.of();

    /**
     * The driver box, {@code null} when the provider lists no driver.
     */
    private ComboBox<DriverChoice> myDriverBox;

    /**
     * An entry of the driver box: a driver id and a version, as they are stored. A {@code null} version is the latest version of the
     * driver; a {@code null} driver id with it is the default - the latest version of the preferred driver.
     */
    private record DriverChoice(@Nullable String driverId, @Nullable String version, @Nonnull LocalizeValue text) {
    }

    @RequiredUIAccess
    public DataSourceConfigurable(Project project, EditableDataSource dataSource, Runnable treeUpdater) {
        super(true, treeUpdater);
        myProject = project;
        myDataSource = dataSource;
        myTreeUpdater = treeUpdater;
    }

    @RequiredUIAccess
    @Override
    public boolean isModified() {
        return isDriverModified() || (myInnerConfigurable != null && myInnerConfigurable.isModified());
    }

    @RequiredUIAccess
    @Override
    public void apply() throws ConfigurationException {
        if (myInnerConfigurable != null) {
            myInnerConfigurable.apply();
        }

        if (isDriverModified()) {
            DriverChoice choice = myDriverBox.getValue();
            myDataSource.getProperties().set(GenericPropertyKeys.DRIVER, choice.driverId());
            myDataSource.getProperties().set(GenericPropertyKeys.DRIVER_VERSION, choice.version());
        }
    }

    @RequiredUIAccess
    @Override
    public void reset() {
        updateName();

        myApplicationAwareBox.setValue(myDataSource.isApplicationAware());

        if (myInnerConfigurable != null) {
            myInnerConfigurable.reset();
        }

        if (myDriverBox != null) {
            myDriverBox.setValue(getStoredChoice());
        }
    }

    @RequiredUIAccess
    @Nullable
    @Override
    protected Component createTopRightComponent(TextBox nameField, Disposable parentUIDisposable) {
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
    public void disposeUIResources() {
        super.disposeUIResources();

        myInnerConfigurable = null;
        myDriverBox = null;
        myDriverChoices = List.of();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.ofNullable(myDataSource.getName());
    }

    @Override
    public void setDisplayName(String name) {
        myDataSource.setName(name);
    }

    @Nullable
    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public EditableDataSource getEditableObject() {
        return null;
    }

    @Override
    public LocalizeValue getBannerSlogan() {
        return LocalizeValue.empty();
    }

    @Nonnull
    @Override
    @RequiredUIAccess
    public Component createOptionsPanel(Disposable parentUIDisposable) {
        if (myInnerConfigurable == null) {
            myInnerConfigurable = myDataSource.getProvider().createConfigurable(myDataSource);
        }

        DockLayout panel = DockLayout.create();
        panel.center(myInnerConfigurable.createUIComponent(parentUIDisposable));

        Button testButton = Button.create(LocalizeValue.localizeTODO("Test Connection"), (event) ->
        {
            try {
                apply();
            }
            catch (ConfigurationException ignored) {
                return;
            }

            try {
                myDataSource.getProvider().validateConfiguration(myDataSource.getProperties());
            }
            catch (DataSourceConfigurationException e) {
                Alerts.okError(e.getMessageValue()).showAsync();
                return;
            }

            DataSourceTransportManager dataSourceTransportManager = DataSourceTransportManager.getInstance(myProject);

            AsyncResult<Void> result = dataSourceTransportManager.testConnection(myDataSource);

            UIAccess uiAccess = UIAccess.current();

            result.doWhenDone(() -> uiAccess.give(() -> Alerts.okInfo("Connection success").showAsync()));

            result.doWhenRejectedWithThrowable(throwable -> {
                if (throwable instanceof ProcessCanceledException) {
                    // canceled no need info
                    return;
                }
                uiAccess.give(() -> Alerts.okError("Connection failed: " + throwable.getMessage()).showAsync());
            });
        });

        DockLayout buttonPanel = DockLayout.create().right(testButton);
        buttonPanel.paddingBuilder().allSet(Space.MEDIUM).apply();

        Component driverRow = createDriverRow();
        if (driverRow == null) {
            panel.bottom(buttonPanel);
        }
        else {
            VerticalLayout bottomPanel = VerticalLayout.create();
            bottomPanel.add(driverRow);
            bottomPanel.add(buttonPanel);
            panel.bottom(bottomPanel);
        }

        return panel;
    }

    @Nullable
    @RequiredUIAccess
    private Component createDriverRow() {
        DataSourceProvider provider = myDataSource.getProvider();
        List<String> driverIds = provider.getDriverIds();
        if (driverIds.isEmpty()) {
            myDriverChoices = List.of();
            myDriverBox = null;
            return null;
        }

        DataSourceDriverRegistryImpl registry = (DataSourceDriverRegistryImpl) DataSourceDriverRegistry.getInstance();

        List<DriverChoice> choices = new ArrayList<>();
        for (int i = 0; i < driverIds.size(); i++) {
            String driverId = driverIds.get(i);
            DriverJson driver = registry.getDriver(provider, driverId);

            // nothing is stored for the latest version of the preferred driver
            String latestId = i == 0 ? null : driverId;
            choices.add(new DriverChoice(latestId, null, DatabaseLocalize.driverLatest(driver.name + " " + driver.drivers.get(0).version)));

            // the listed versions, then the installed ones the description no longer lists
            Set<String> versions = new LinkedHashSet<>();
            for (DriverVersionJson version : driver.drivers) {
                versions.add(version.version);
            }
            versions.addAll(registry.getInstalledVersions(driverId));

            for (String version : versions) {
                choices.add(new DriverChoice(driverId, version, LocalizeValue.of(driver.name + " " + version)));
            }
        }

        ComboBox<DriverChoice> driverBox = ComboBox.create(choices);
        driverBox.setTextRenderer(choice -> choice == null ? LocalizeValue.empty() : choice.text());
        myDriverChoices = choices;
        myDriverBox = driverBox;

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(DatabaseLocalize.labelDriver(), driverBox);
        Component driverRow = builder.build();
        driverRow.paddingBuilder().horizontalSet(Space.MEDIUM).topSet(Space.MEDIUM).apply();
        return driverRow;
    }

    /**
     * @return the entry of the stored driver id and version, mapped as the registry maps them: an id the provider does not list is the
     * preferred driver, and a version which is neither listed nor installed is the latest one
     */
    @Nonnull
    private DriverChoice getStoredChoice() {
        PropertiesHolder properties = myDataSource.getProperties();
        String storedId = properties.get(GenericPropertyKeys.DRIVER);
        List<String> driverIds = myDataSource.getProvider().getDriverIds();

        String driverId = storedId != null && driverIds.contains(storedId) ? storedId : driverIds.get(0);

        // a version belongs to the driver it was selected with
        boolean ownVersion = storedId == null || storedId.equals(driverId);
        String version = ownVersion ? properties.get(GenericPropertyKeys.DRIVER_VERSION) : null;
        if (version != null) {
            for (DriverChoice choice : myDriverChoices) {
                if (driverId.equals(choice.driverId()) && version.equals(choice.version())) {
                    return choice;
                }
            }
        }

        String latestId = driverId.equals(driverIds.get(0)) ? null : driverId;
        for (DriverChoice choice : myDriverChoices) {
            if (choice.version() == null && Objects.equals(choice.driverId(), latestId)) {
                return choice;
            }
        }
        return myDriverChoices.get(0);
    }

    private boolean isDriverModified() {
        if (myDriverBox == null) {
            return false;
        }

        DriverChoice selected = myDriverBox.getValue();
        DriverChoice stored = getStoredChoice();
        return selected != null &&
            (!Objects.equals(selected.driverId(), stored.driverId()) || !Objects.equals(selected.version(), stored.version()));
    }
}
