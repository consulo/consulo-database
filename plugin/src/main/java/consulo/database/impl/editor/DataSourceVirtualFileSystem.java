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
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.BaseVirtualFileSystem;
import consulo.virtualFileSystem.NonPhysicalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.archive.ArchiveFileSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
@ExtensionImpl
public class DataSourceVirtualFileSystem extends BaseVirtualFileSystem implements NonPhysicalFileSystem
{
	public static final String PROTOCOL = "db";

	@Nonnull
	public static DataSourceVirtualFileSystem getInstance()
	{
		return (DataSourceVirtualFileSystem) VirtualFileManager.getInstance().getFileSystem(PROTOCOL);
	}

	@Nonnull
	public VirtualFile createFile(DataSource dataSource, String dbName, String childId)
	{
		VirtualFile file = findFileByPath(dataSource.getId() + ArchiveFileSystem.ARCHIVE_SEPARATOR + dbName + "/" + childId);
		assert file != null;
		return file;
	}

	@Nonnull
	@Override
	public String getProtocol()
	{
		return PROTOCOL;
	}

	@Nullable
	@Override
	public VirtualFile findFileByPath(@Nonnull String path)
	{
		String[] values = path.split(ArchiveFileSystem.ARCHIVE_SEPARATOR);

		UUID uuid = UUID.fromString(values[0]);

		List<String> dbAndChildId = StringUtil.split(values[1], "/");
		if(dbAndChildId.size() != 2)
		{
			return null;
		}

		String dbName = dbAndChildId.get(0);
		String childId = dbAndChildId.get(1);

		for(Project project : ProjectManager.getInstance().getOpenProjects())
		{
			DataSource dataSource = ReadAction.compute(() -> DataSourceManager.getInstance(project).findDataSource(uuid));
			if(dataSource != null)
			{
				return new DataSourceVirtualFile(dataSource, dbName, childId, this);
			}
		}
		return null;
	}

	@Override
	public void refresh(boolean asynchronous)
	{

	}

	@Nullable
	@Override
	public VirtualFile refreshAndFindFileByPath(@Nonnull String path)
	{
		return findFileByPath(path);
	}
}
