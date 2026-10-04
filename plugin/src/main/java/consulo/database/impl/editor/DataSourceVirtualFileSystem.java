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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.virtualFileSystem.BaseVirtualFileSystem;
import consulo.virtualFileSystem.NonPhysicalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.archive.ArchiveFileSystem;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * File path: {@code <data source id>!/<escaped db name>/<escaped child id>}.
 * <p>
 * The db name and the child id are escaped (see {@link #escapePathPart(String)}), because they may contain {@code /}
 * (for example a MongoDB collection name).
 *
 * @author VISTALL
 * @since 2020-08-19
 */
@ExtensionImpl
public class DataSourceVirtualFileSystem extends BaseVirtualFileSystem implements NonPhysicalFileSystem {
    public static final String PROTOCOL = "db";

    private static final char ESCAPE_CHAR = '%';
    private static final String ESCAPED_ESCAPE_CHAR = "%25";
    private static final String ESCAPED_SLASH = "%2F";

    public static DataSourceVirtualFileSystem getInstance() {
        return (DataSourceVirtualFileSystem) VirtualFileManager.getInstance().getFileSystem(PROTOCOL);
    }

    public VirtualFile createFile(DataSource dataSource, String dbName, String childId) {
        return new DataSourceVirtualFile(dataSource, dbName, childId, this);
    }

    public static String buildPath(DataSource dataSource, String dbName, String childId) {
        return dataSource.getId() + ArchiveFileSystem.ARCHIVE_SEPARATOR + escapePathPart(dbName) + "/" + escapePathPart(childId);
    }

    /**
     * Escapes {@code %} as {@code %25} and {@code /} as {@code %2F}, so the escaped value never contains {@code /}
     */
    static String escapePathPart(String value) {
        if (value.indexOf(ESCAPE_CHAR) < 0 && value.indexOf('/') < 0) {
            return value;
        }

        StringBuilder builder = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ESCAPE_CHAR) {
                builder.append(ESCAPED_ESCAPE_CHAR);
            }
            else if (c == '/') {
                builder.append(ESCAPED_SLASH);
            }
            else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * Reverse of {@link #escapePathPart(String)}. Any other {@code %} sequence is kept as is - paths stored before
     * the escaping was introduced contain the raw names
     */
    static String unescapePathPart(String value) {
        if (value.indexOf(ESCAPE_CHAR) < 0) {
            return value;
        }

        StringBuilder builder = new StringBuilder(value.length());
        int i = 0;
        while (i < value.length()) {
            if (value.startsWith(ESCAPED_ESCAPE_CHAR, i)) {
                builder.append(ESCAPE_CHAR);
                i += ESCAPED_ESCAPE_CHAR.length();
            }
            else if (value.regionMatches(true, i, ESCAPED_SLASH, 0, ESCAPED_SLASH.length())) {
                builder.append('/');
                i += ESCAPED_SLASH.length();
            }
            else {
                builder.append(value.charAt(i));
                i++;
            }
        }
        return builder.toString();
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public @Nullable VirtualFile findFileByPath(String path) {
        int separatorIndex = path.indexOf(ArchiveFileSystem.ARCHIVE_SEPARATOR);
        if (separatorIndex < 0) {
            return null;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(path.substring(0, separatorIndex));
        }
        catch (IllegalArgumentException e) {
            return null;
        }

        String dbAndChildId = path.substring(separatorIndex + ArchiveFileSystem.ARCHIVE_SEPARATOR.length());

        // both parts are escaped - so there is exactly one '/'
        int slashIndex = dbAndChildId.indexOf('/');
        if (slashIndex < 0 || dbAndChildId.indexOf('/', slashIndex + 1) >= 0) {
            return null;
        }

        String dbName = unescapePathPart(dbAndChildId.substring(0, slashIndex));
        String childId = unescapePathPart(dbAndChildId.substring(slashIndex + 1));

        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            DataSource dataSource = ReadAction.compute(() -> DataSourceManager.getInstance(project).findDataSource(uuid));
            if (dataSource != null) {
                return createFile(dataSource, dbName, childId);
            }
        }
        return null;
    }

    @Override
    public void refresh(boolean asynchronous) {
    }

    @Override
    public @Nullable VirtualFile refreshAndFindFileByPath(String path) {
        return findFileByPath(path);
    }
}
