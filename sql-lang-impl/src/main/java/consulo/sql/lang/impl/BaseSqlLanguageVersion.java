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

import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.TokenSet;
import consulo.lang.LanguageVersionWithParsing;
import consulo.sql.lang.api.SqlLanguageVersion;
import consulo.sql.lang.impl.lexer.SqlLexer;
import consulo.sql.lang.impl.parser.SqlParser;

import javax.annotation.Nonnull;

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
		return TokenSet.EMPTY;
	}

	@Nonnull
	@Override
	public TokenSet getStringLiteralElements()
	{
		return TokenSet.EMPTY;
	}
}
