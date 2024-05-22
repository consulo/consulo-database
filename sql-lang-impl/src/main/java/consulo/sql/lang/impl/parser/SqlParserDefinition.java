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

package consulo.sql.lang.impl.parser;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IFileElementType;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersionableParserDefinition;
import consulo.sql.lang.api.SqlLanguage;
import consulo.sql.lang.impl.psi.SqlFileImpl;
import consulo.sql.lang.impl.psi.SqlKeywordTokenTypes;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22/10/2021
 */
@ExtensionImpl
public class SqlParserDefinition extends LanguageVersionableParserDefinition
{
	private static final IFileElementType FILE_ELEMENT_TYPE = new IFileElementType("SQL_FILE", SqlLanguage.INSTANCE);

	static
	{
		SqlKeywordTokenTypes.init();
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return SqlLanguage.INSTANCE;
	}

	@Nonnull
	@Override
	public IFileElementType getFileNodeType()
	{
		return FILE_ELEMENT_TYPE;
	}

	@Nonnull
	@Override
	public PsiFile createFile(@Nonnull FileViewProvider fileViewProvider)
	{
		return new SqlFileImpl(fileViewProvider);
	}
}
