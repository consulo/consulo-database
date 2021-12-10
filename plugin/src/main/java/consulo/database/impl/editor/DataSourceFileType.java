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

import com.intellij.openapi.fileTypes.FileType;
import consulo.database.icon.DatabaseIconGroup;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-09-07
 */
public class DataSourceFileType implements FileType
{
	public static final DataSourceFileType INSTANCE = new DataSourceFileType();

	@Nonnull
	@Override
	public String getId()
	{
		return "DATASOURCE";
	}

	@Nonnull
	@Override
	public LocalizeValue getDescription()
	{
		return LocalizeValue.of();
	}

	@Nonnull
	@Override
	public String getDefaultExtension()
	{
		return "";
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return DatabaseIconGroup.nodesTable();
	}

	@Override
	public boolean isBinary()
	{
		return true;
	}
}
