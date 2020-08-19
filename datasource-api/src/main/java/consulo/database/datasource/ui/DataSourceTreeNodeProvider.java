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

package consulo.database.datasource.ui;

import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import consulo.database.datasource.model.DataSource;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public interface DataSourceTreeNodeProvider
{
	ExtensionPointName<DataSourceTreeNodeProvider> EP_NAME = ExtensionPointName.create("consulo.database.treeNodeProvider");

	void fillTreeNodes(@Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull Consumer<AbstractTreeNode<?>> consumer);
}
