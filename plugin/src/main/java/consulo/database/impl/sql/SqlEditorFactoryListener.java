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

package consulo.database.impl.sql;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorEx;
import consulo.codeEditor.event.EditorFactoryEvent;
import consulo.codeEditor.event.EditorFactoryListener;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.sql.lang.api.SqlFileType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-10-31
 */
@ExtensionImpl
public class SqlEditorFactoryListener implements EditorFactoryListener
{
	@Override
	public void editorCreated(@Nonnull EditorFactoryEvent event)
	{
		Editor editor = event.getEditor();
		if(editor instanceof EditorEx)
		{
			Project project = editor.getProject();
			if(project == null)
			{
				return;
			}

			PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

			if(psiFile != null && psiFile.getViewProvider().getFileType() == SqlFileType.INSTANCE)
			{
				SqlExecutePanel panel = new SqlExecutePanel(project, editor, psiFile.getName());

				editor.setHeaderComponent(panel.getPanel());
			}
		}
	}
}
