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
import consulo.application.dumb.DumbAware;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorPolicy;
import consulo.fileEditor.FileEditorProvider;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
@ExtensionImpl
public class DataSourceFileEditorProvider implements FileEditorProvider, DumbAware
{
	@Override
	public boolean accept(@Nonnull Project project, @Nonnull VirtualFile file)
	{
		return file instanceof DataSourceVirtualFile;
	}

	@Nonnull
	@Override
	public FileEditor createEditor(@Nonnull Project project, @Nonnull VirtualFile file)
	{
		return new DataSourceFileEditor(project, (DataSourceVirtualFile) file);
	}

	@Nonnull
	@Override
	public String getEditorTypeId()
	{
		return "datasource";
	}

	@Nonnull
	@Override
	public FileEditorPolicy getPolicy()
	{
		return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
	}
}
