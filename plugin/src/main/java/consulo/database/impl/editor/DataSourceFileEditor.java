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

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.util.dataholder.UserDataHolderBase;
import kava.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DataSourceFileEditor extends UserDataHolderBase implements FileEditor
{
	private JPanel myTargetPanel;

	public DataSourceFileEditor(Project project, VirtualFile file)
	{
		myTargetPanel = new JPanel();
	}

	@Nonnull
	@Override
	public JComponent getComponent()
	{
		return myTargetPanel;
	}

	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent()
	{
		return myTargetPanel;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "datasource";
	}

	@Nonnull
	@Override
	public FileEditorState getState(@Nonnull FileEditorStateLevel fileEditorStateLevel)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void setState(@Nonnull FileEditorState fileEditorState)
	{

	}

	@Override
	public boolean isModified()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void selectNotify()
	{

	}

	@Override
	public void deselectNotify()
	{

	}

	@Override
	public void addPropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener)
	{

	}

	@Override
	public void removePropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener)
	{

	}

	@Nullable
	@Override
	public FileEditorLocation getCurrentLocation()
	{
		return null;
	}

	@Override
	public void dispose()
	{

	}
}
