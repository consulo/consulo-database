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
package consulo.database.datasource.configurable;

import consulo.configurable.SimpleConfigurableByProperties;
import consulo.database.datasource.model.DataSource;
import consulo.ui.PasswordBox;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Binds a password box to a secure value of a data source without touching the credential store on the UI thread.
 * <p>
 * The stored value is read asynchronously when the box is reset, and fills the box once the store answers - unless the user typed
 * meanwhile. Until then nothing counts as modified, and a box nobody typed into applies nothing, so a slow store never replaces a
 * password with an empty one. An unchanged value is not stored again.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public final class SecureStringProperty {
    private final EditablePropertiesHolder myHolder;
    private final GenericPropertyKey<SecureString> myKey;
    private final DataSource myDataSource;
    private final PasswordBox myBox;

    private @Nullable String myStoredValue;
    private boolean myLoaded;
    private boolean myLoading;
    private boolean myEdited;
    private boolean myUpdatingBox;

    private SecureStringProperty(EditablePropertiesHolder holder,
                                 GenericPropertyKey<SecureString> key,
                                 DataSource dataSource,
                                 PasswordBox box) {
        myHolder = holder;
        myKey = key;
        myDataSource = dataSource;
        myBox = box;

        box.addValueListener(event -> {
            if (!myUpdatingBox) {
                myEdited = true;
            }
        });
    }

    public static void bind(SimpleConfigurableByProperties.PropertyBuilder builder,
                            PasswordBox box,
                            EditablePropertiesHolder holder,
                            GenericPropertyKey<SecureString> key,
                            DataSource dataSource) {
        SecureStringProperty property = new SecureStringProperty(holder, key, dataSource, box);
        builder.add(property::getBoxValue, property::reset, property::getStoredValue, property::apply);
    }

    private String getBoxValue() {
        return notNullize(myBox.getValue());
    }

    /**
     * While the stored value is not known, the value of the box - nothing is modified.
     */
    private String getStoredValue() {
        return myLoaded ? notNullize(myStoredValue) : getBoxValue();
    }

    @RequiredUIAccess
    private void reset(String ignored) {
        myEdited = false;
        if (myLoaded) {
            setBoxValue(myStoredValue);
        }
        else {
            load();
        }
    }

    private void apply(String value) {
        if (!myLoaded && !myEdited) {
            // the stored value is not known yet, and nobody typed into the box
            return;
        }
        if (myLoaded && Objects.equals(value, notNullize(myStoredValue))) {
            return;
        }

        myHolder.set(myKey, SecureString.raw(value));
        myStoredValue = value;
        myLoaded = true;
    }

    @RequiredUIAccess
    private void load() {
        if (myLoading) {
            return;
        }
        myLoading = true;

        SecureString secureString = myHolder.get(myKey);
        CompletableFuture<String> value = secureString == null
            ? CompletableFuture.completedFuture(null)
            : secureString.getValueAsync(myDataSource);

        value.whenCompleteAsync((stored, error) -> {
            myLoading = false;
            myLoaded = true;
            myStoredValue = error == null ? stored : null;
            if (!myEdited) {
                setBoxValue(myStoredValue);
            }
        }, UIAccess.current());
    }

    @RequiredUIAccess
    private void setBoxValue(@Nullable String value) {
        myUpdatingBox = true;
        try {
            myBox.setValue(notNullize(value));
        }
        finally {
            myUpdatingBox = false;
        }
    }

    private static String notNullize(@Nullable String value) {
        return value == null ? "" : value;
    }
}
