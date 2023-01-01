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

package consulo.database.impl.toolWindow.node;

import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.tree.PresentationData;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseRootNode extends AbstractTreeNode<Object>
{
	public DatabaseRootNode(Project project)
	{
		super(project, "root");
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		DataSourceManager dataSourceManager = DataSourceManager.getInstance(myProject);

		List<? extends DataSource> dataSources = dataSourceManager.getDataSources();

		return dataSources.stream().map(o -> new DatabaseSourceNode(myProject, o)).collect(Collectors.toList());
	}

	@Override
	protected void update(PresentationData presentationData)
	{

	}
}
