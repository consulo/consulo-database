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

import consulo.database.datasource.model.DataSource;
import consulo.language.file.light.LightVirtualFile;
import consulo.virtualFileSystem.VirtualFileSystem;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DataSourceVirtualFile extends LightVirtualFile {
    private final DataSource myDataSource;
    private final String myDatabaseName;
    private final String myChildId;
    private final VirtualFileSystem myVirtualFileSystem;
    private final String myPath;

    public DataSourceVirtualFile(DataSource dataSource, String databaseName, String childId, VirtualFileSystem virtualFileSystem) {
        super("[" + dataSource.getName() + "] " + childId, DataSourceFileType.INSTANCE, "");

        myDataSource = dataSource;
        myDatabaseName = databaseName;
        myChildId = childId;
        myVirtualFileSystem = virtualFileSystem;
        myPath = DataSourceVirtualFileSystem.buildPath(dataSource, databaseName, childId);
    }

    public String getDatabaseName() {
        return myDatabaseName;
    }

    public String getChildId() {
        return myChildId;
    }

    public DataSource getDataSource() {
        return myDataSource;
    }

    @Override
    public VirtualFileSystem getFileSystem() {
        return myVirtualFileSystem;
    }

    /**
     * The db name and the child id are escaped, see {@link DataSourceVirtualFileSystem#buildPath}
     */
    @Override
    public String getPath() {
        return myPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataSourceVirtualFile) {
            return getPath().equals(((DataSourceVirtualFile) obj).getPath());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }
}
