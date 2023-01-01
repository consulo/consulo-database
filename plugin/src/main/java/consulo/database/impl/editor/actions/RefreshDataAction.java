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

package consulo.database.impl.editor.actions;

import consulo.application.AllIcons;
import consulo.database.impl.editor.DataSourceFileEditor;
import consulo.database.impl.editor.DataSourceFileEditorKeys;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-30
 */
public class RefreshDataAction extends DumbAwareAction
{
	public RefreshDataAction()
	{
		super("Reload data", null, AllIcons.Actions.Refresh);
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		DataSourceFileEditor editor = e.getRequiredData(DataSourceFileEditorKeys.EDITOR);

		editor.loadData(UIAccess.current());
	}
}
