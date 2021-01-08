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

package consulo.sql.lang.api;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-10-31
 */
public class SqlLanguage extends Language
{
	public static final SqlLanguage INSTANCE = new SqlLanguage();

	public SqlLanguage()
	{
		super(PlainTextLanguage.INSTANCE, "SQL");
	}

	@Nullable
	@Override
	public LanguageFileType getAssociatedFileType()
	{
		return SqlFileType.INSTANCE;
	}
}
