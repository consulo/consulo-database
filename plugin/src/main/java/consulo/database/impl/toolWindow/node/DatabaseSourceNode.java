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
import consulo.application.Application;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.ui.DataSourceTreeNodeProvider;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentationData;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseSourceNode extends AbstractTreeNode<DataSource>
{
	public DatabaseSourceNode(Project project, @Nonnull DataSource value)
	{
		super(project, value);
	}

	@Override
	protected void update(PresentationData presentationData)
	{
		DataSource dataSource = getValue();

		presentationData.addText(dataSource.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		Image image = dataSource.getProvider().getIcon();
		if(dataSource.isApplicationAware())
		{
			image = ImageEffects.layered(image, PlatformIconGroup.nodesSymlink());
		}
		presentationData.setIcon(image);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends AbstractTreeNode> getChildren()
	{
		DataSource value = getValue();
		if(value instanceof EditableDataSource)
		{
			return Collections.emptyList();
		}
		List<AbstractTreeNode<?>> result = new ArrayList<>();
		for(DataSourceTreeNodeProvider provider : DataSourceTreeNodeProvider.EP_NAME.getExtensionList(Application.get()))
		{
			if(provider.accept(value))
			{
				provider.fillTreeNodes(myProject, value, result::add);
				break;
			}
		}
		return result;
	}

	@Nonnull
	@Override
	public String toString()
	{
		return getValue().getId().toString();
	}
}
