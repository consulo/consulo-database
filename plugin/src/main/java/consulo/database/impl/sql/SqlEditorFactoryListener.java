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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import consulo.sql.lang.api.SqlFileType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-10-31
 */
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
				SqlExecutePanel panel = new SqlExecutePanel(project, editor);

				editor.setHeaderComponent(panel.getPanel());
			}
		}
	}
}
