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

package consulo.database.impl.editor.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import consulo.database.impl.editor.DataSourceFileEditor;
import consulo.database.impl.editor.DataSourceFileEditorKeys;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 09/12/2021
 */
public class PageAction extends AnAction implements CustomComponentAction
{
	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{

	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		DataSourceFileEditor editor = e.getRequiredData(DataSourceFileEditorKeys.EDITOR);

		JBLabel component = (JBLabel) e.getPresentation().getClientProperty(CustomComponentAction.COMPONENT_KEY);
		if(component == null)
		{
			return;
		}
		component.setText(editor.getRowsCount() + " rows");
	}

	@Nonnull
	@Override
	public JComponent createCustomComponent(@Nonnull Presentation presentation, @Nonnull String place)
	{
		JBLabel label = new JBLabel("1 row");
		label.setBorder(JBUI.Borders.empty(0, 5));
		label.setFont(UIUtil.getLabelFont(UIUtil.FontSize.MINI));
		return label;
	}
}
