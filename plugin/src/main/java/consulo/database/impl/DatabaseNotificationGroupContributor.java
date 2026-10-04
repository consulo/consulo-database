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

package consulo.database.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.database.impl.localize.DatabaseLocalize;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationGroupContributor;

import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-10-04
 */
@ExtensionImpl
public class DatabaseNotificationGroupContributor implements NotificationGroupContributor {
    public static final NotificationGroup DATABASE_GROUP = NotificationGroup.balloonGroup("consulo.database", DatabaseLocalize.toolwindowTitle());

    @Override
    public void contribute(Consumer<NotificationGroup> consumer) {
        consumer.accept(DATABASE_GROUP);
    }
}
