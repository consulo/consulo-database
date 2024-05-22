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

import consulo.application.presentation.TypePresentationProvider;
import consulo.database.icon.DatabaseIconGroup;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DataSourceFilePresentation extends TypePresentationProvider<DataSourceVirtualFile>
{
	@Nonnull
	@Override
	public Class<DataSourceVirtualFile> getItemClass()
	{
		return DataSourceVirtualFile.class;
	}

	@Nullable
	@Override
	public Image getIcon(DataSourceVirtualFile f)
	{
		return DatabaseIconGroup.nodesTable();
	}

	@Nullable
	@Override
	public String getName(DataSourceVirtualFile file)
	{
		return file.getName();
	}
}
