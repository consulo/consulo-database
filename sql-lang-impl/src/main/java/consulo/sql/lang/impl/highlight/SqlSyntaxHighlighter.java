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

package consulo.sql.lang.impl.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.tree.IElementType;
import consulo.fileTypes.LanguageVersionableSyntaxHighlighter;
import consulo.lang.LanguageVersion;
import consulo.sql.lang.impl.BaseSqlLanguageVersion;
import consulo.sql.lang.impl.psi.SqlKeywordElementType;
import consulo.sql.lang.impl.psi.SqlTokenType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22/10/2021
 */
public class SqlSyntaxHighlighter extends LanguageVersionableSyntaxHighlighter
{
	private static Map<IElementType, TextAttributesKey> ourTextAttributes = new HashMap<>();

	static
	{
		safeMap(ourTextAttributes, SqlTokenType.LPAR, DefaultLanguageHighlighterColors.PARENTHESES);
		safeMap(ourTextAttributes, SqlTokenType.RPAR, DefaultLanguageHighlighterColors.PARENTHESES);
		safeMap(ourTextAttributes, SqlTokenType.COMMA, DefaultLanguageHighlighterColors.COMMA);
		safeMap(ourTextAttributes, SqlTokenType.IDENTIFIER, DefaultLanguageHighlighterColors.IDENTIFIER);
		safeMap(ourTextAttributes, SqlTokenType.SINGLE_QUOTED_LITERAL, DefaultLanguageHighlighterColors.STRING);
		safeMap(ourTextAttributes, SqlTokenType.NUMBER, DefaultLanguageHighlighterColors.NUMBER);
	}

	public SqlSyntaxHighlighter(LanguageVersion languageVersion)
	{
		super(languageVersion);
	}

	@Override
	public Lexer getHighlightingLexer(LanguageVersion languageVersion)
	{
		BaseSqlLanguageVersion sqlLanguageVersion = (BaseSqlLanguageVersion) languageVersion;
		return sqlLanguageVersion.createLexer();
	}

	@Nonnull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
	{
		if(SqlKeywordElementType.isKeyword(tokenType))
		{
			return pack(DefaultLanguageHighlighterColors.KEYWORD);
		}

		return pack(ourTextAttributes.get(tokenType));
	}
}
