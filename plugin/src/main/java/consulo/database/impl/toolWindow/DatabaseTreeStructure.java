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

package consulo.database.impl.toolWindow;

import consulo.database.impl.toolWindow.node.DatabaseRootNode;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.awt.tree.TreeNode;
import consulo.ui.ex.tree.AbstractTreeStructure;
import consulo.ui.ex.tree.NodeDescriptor;
import consulo.util.collection.ArrayUtil;

import jakarta.annotation.Nonnull;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public class DatabaseTreeStructure extends AbstractTreeStructure
{
	private DatabaseRootNode myRootNode;

	public DatabaseTreeStructure(Project project)
	{
		myRootNode = new DatabaseRootNode(project);
	}

	@Nonnull
	@Override
	public Object getRootElement()
	{
		return myRootNode;
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
