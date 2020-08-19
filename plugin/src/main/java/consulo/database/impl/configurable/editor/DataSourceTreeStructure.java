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

package consulo.database.impl.configurable.editor;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.impl.configurable.editor.node.DataSourceEditorRootNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceTreeStructure extends AbstractTreeStructureBase
{
	private final DataSourceModel myDataSourceModel;
	private final DataSourceEditorRootNode myRoot;

	public DataSourceTreeStructure(Project project, DataSourceModel dataSourceModel)
	{
		super(project);
		myDataSourceModel = dataSourceModel;
		myRoot = new DataSourceEditorRootNode(myProject, dataSourceModel);
	}

	@Nullable
	@Override
	public List<TreeStructureProvider> getProviders()
	{
		return null;
	}

	@Nonnull
	@Override
	public Object getRootElement()
	{
		return myRoot;
	}

	@Override
	public void commit()
	{

	}

	@Override
	public boolean hasSomethingToCommit()
	{
		return false;
	}
}
