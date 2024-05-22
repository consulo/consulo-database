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

package consulo.database.impl.configurable.editor.node;

import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.impl.toolWindow.node.DatabaseProviderNode;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.tree.PresentationData;
import consulo.util.lang.ObjectUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceEditorRootNode extends AbstractTreeNode<Object>
{
	private final DataSourceModel myDataSourceModel;

	public DataSourceEditorRootNode(Project project, DataSourceModel dataSourceModel)
	{
		super(project, ObjectUtil.NULL);
		myDataSourceModel = dataSourceModel;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		return DatabaseProviderNode.split(getProject(), myDataSourceModel);
	}

	@Override
	protected void update(PresentationData presentation)
	{
	}
}
