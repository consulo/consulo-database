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

package consulo.sql.lang.impl;

import consulo.language.ast.TokenSet;
import consulo.language.lexer.Lexer;
import consulo.language.parser.PsiParser;
import consulo.language.version.LanguageVersionWithParsing;
import consulo.sql.lang.api.SqlLanguageVersion;
import consulo.sql.lang.impl.lexer.SqlLexer;
import consulo.sql.lang.impl.parser.SqlParser;
import consulo.sql.lang.impl.psi.SqlTokenType;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22/10/2021
 */
public class BaseSqlLanguageVersion extends SqlLanguageVersion implements LanguageVersionWithParsing
{
	private TokenSet myReservedKeywords = TokenSet.EMPTY;

	public BaseSqlLanguageVersion(@Nonnull String id, @Nonnull String name)
	{
		super(id, name);
	}

	public void addReservedKeywords(TokenSet tokenSet)
	{
		myReservedKeywords = TokenSet.orSet(myReservedKeywords, tokenSet);
	}

	@Nonnull
	public TokenSet getReservedKeywords()
	{
		return myReservedKeywords;
	}

	@Nonnull
	@Override
	public Lexer createLexer()
	{
		return new SqlLexer(this);
	}

	@Nonnull
	@Override
	public PsiParser createParser()
	{
		return new SqlParser();
	}

	@Nonnull
	@Override
	public TokenSet getWhitespaceTokens()
	{
		return TokenSet.WHITE_SPACE;
	}

	@Nonnull
	@Override
	public TokenSet getCommentTokens()
	{
		return SqlTokenType.COMMENTS;
	}

	@Nonnull
	@Override
	public TokenSet getStringLiteralElements()
	{
		return SqlTokenType.STRINGS;
	}
}
