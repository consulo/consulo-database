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

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.containers.MultiMap;
import consulo.annotation.access.RequiredReadAction;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceModel;
import consulo.database.datasource.provider.DataSourceProvider;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public class DatabaseProviderNode extends AbstractTreeNode<DataSourceProvider>
{
	@Nonnull
	public static List<? extends AbstractTreeNode> split(@Nonnull Project project, @Nonnull DataSourceModel dataSourceModel)
	{
		MultiMap<DataSourceProvider, DataSource> providers = MultiMap.createLinked();
		for(DataSource dataSource : dataSourceModel.getDataSources())
		{
			providers.putValue(dataSource.getProvider(), dataSource);
		}
		return providers.entrySet().stream().map(e -> new DatabaseProviderNode(project, e.getKey(), e.getValue())).collect(Collectors.toList());
	}

	@Nonnull
	private final Collection<DataSource> myDataSources;

	public DatabaseProviderNode(Project project, @Nonnull DataSourceProvider value, @Nonnull Collection<DataSource> dataSources)
	{
		super(project, value);
		myDataSources = dataSources;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		return myDataSources.stream().map(o -> new DatabaseSourceNode(getProject(), o)).collect(Collectors.toList());
	}

	@Override
	protected void update(PresentationData presentation)
	{
		DataSourceProvider value = getValue();
		presentation.addText(value.getName().getValue(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
		presentation.setIcon(AllIcons.Nodes.Folder);
	}
}
