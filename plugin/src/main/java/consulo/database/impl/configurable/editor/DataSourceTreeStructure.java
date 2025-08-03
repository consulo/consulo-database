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

import consulo.database.datasource.model.DataSourceModel;
import consulo.database.impl.configurable.editor.node.DataSourceEditorRootNode;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.tree.AbstractTreeStructure;
import consulo.ui.ex.tree.NodeDescriptor;
import consulo.ui.ex.tree.TreeNode;
import consulo.util.collection.ArrayUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceTreeStructure extends AbstractTreeStructure
{
	private final DataSourceEditorRootNode myRoot;

	public DataSourceTreeStructure(Project project, DataSourceModel dataSourceModel)
	{
		myRoot = new DataSourceEditorRootNode(project, dataSourceModel);
	}

	@Nonnull
	@Override
	public Object[] getChildElements(@Nonnull Object element)
	{
		TreeNode<?> treeNode = (TreeNode) element;
		Collection<? extends TreeNode> elements = treeNode.getChildren();
		elements.forEach(node -> node.setParent(treeNode));
		return ArrayUtil.toObjectArray(elements);
	}

	@Override
	public boolean isValid(@Nonnull Object element)
	{
		return element instanceof AbstractTreeNode;
	}

	@Override
	public Object getParentElement(@Nonnull Object element)
	{
		if(element instanceof TreeNode)
		{
			return ((TreeNode) element).getParent();
		}
		return null;
	}

	@Override
	@Nonnull
	public NodeDescriptor createDescriptor(@Nonnull final Object element, final NodeDescriptor parentDescriptor)
	{
		return (NodeDescriptor) element;
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
