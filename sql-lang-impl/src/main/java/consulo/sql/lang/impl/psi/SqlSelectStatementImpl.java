/*
 * Copyright 2013-2021 consulo.io
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

package consulo.sql.lang.impl.psi;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.sql.lang.api.psi.SqlSelectStatement;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09/12/2021
 */
public class SqlSelectStatementImpl extends ASTWrapperPsiElement implements SqlSelectStatement
{
	public SqlSelectStatementImpl(@Nonnull ASTNode node)
	{
		super(node);
	}
}
