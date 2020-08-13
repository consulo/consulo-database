package consulo.database.impl.editor;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceConfigurable implements Configurable
{
	@RequiredUIAccess
	@Override
	public boolean isModified()
	{
		return false;
	}

	@RequiredUIAccess
	@Override
	public void apply() throws ConfigurationException
	{

	}
}
