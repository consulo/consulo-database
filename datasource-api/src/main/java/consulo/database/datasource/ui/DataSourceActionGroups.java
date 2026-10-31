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
package consulo.database.datasource.ui;

/**
 * Ids of the action groups of the database tool window, which other plugins add their actions to.
 *
 * @author VISTALL
 * @since 2026-10-31
 */
public interface DataSourceActionGroups {
    /**
     * The first part of the popup of the "Add Datasource" button, before the actions creating a data source of each provider
     */
    String ADD_DATA_SOURCE = "DataSource.AddDataSourceGroup";

    /**
     * The context menu of the data source tree; {@link DataSourceKeys#DATASOURCE} and {@link DataSourceKeys#OPENABLE_NODE} give the
     * selection
     */
    String TREE_POPUP = "DataSource.TreePopupGroup";
}
