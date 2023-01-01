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

import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.util.collection.CharSequenceHashingStrategy;
import consulo.util.collection.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22/10/2021
 */
public class SqlKeywordElementType extends IElementType
{
	private static final Map<CharSequence, SqlKeywordElementType> ourRegistry = Maps.newConcurrentHashMap(CharSequenceHashingStrategy.CASE_INSENSITIVE);
	private static final Map<IElementType, String> ourRegistry2 = Maps.newConcurrentHashMap();

	@Nonnull
	private final String myKeyword;

	public SqlKeywordElementType(@Nonnull String keyword, @Nullable Language language)
	{
		super(keyword.toUpperCase(Locale.ROOT) + "_KEYWORD", language);
		myKeyword = keyword;

		if(ourRegistry.put(keyword, this) != null)
		{
			throw new IllegalArgumentException("Already registered keyword " + keyword);
		}
		else
		{
			ourRegistry2.put(this, keyword);
		}
	}

	@Nullable
	public static IElementType toKeyword(@Nonnull CharSequence keywordText)
	{
		return ourRegistry.get(keywordText);
	}

	public static boolean isKeyword(@Nonnull IElementType elementType)
	{
		return ourRegistry2.get(elementType) != null;
	}

	@Nonnull
	public String getKeyword()
	{
		return myKeyword;
	}
}
