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

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import consulo.sql.lang.api.SqlLanguage;

/**
 * @author VISTALL
 * @since 22/10/2021
 */
public interface SqlTokenType extends TokenType
{
	IElementType IDENTIFIER = new IElementType("SQL_IDENTIFIER", SqlLanguage.INSTANCE);

	IElementType COMMA = new IElementType("SQL_COMMA", SqlLanguage.INSTANCE);

	IElementType LPAR = new IElementType("SQL_LPAR", SqlLanguage.INSTANCE);

	IElementType RPAR = new IElementType("SQL_RPAR", SqlLanguage.INSTANCE);

	IElementType NUMBER = new IElementType("SQL_NUMBER", SqlLanguage.INSTANCE);

	IElementType SINGLE_QUOTED_LITERAL = new IElementType("SQL_SINGLE_QUOTED_LITERAL", SqlLanguage.INSTANCE);
}
