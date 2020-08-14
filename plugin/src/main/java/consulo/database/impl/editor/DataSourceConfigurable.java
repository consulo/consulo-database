package consulo.database.impl.editor;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import consulo.database.datasource.EditableDataSource;
import consulo.ui.annotation.RequiredUIAccess;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceConfigurable extends NamedConfigurable<EditableDataSource>
{
	private final EditableDataSource myDataSource;

	public DataSourceConfigurable(EditableDataSource dataSource, Runnable treeUpdater)
	{
		super(true, treeUpdater);
		myDataSource = dataSource;
	}

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

	@Nls
	@Override
	public String getDisplayName()
	{
		return myDataSource.getName();
	}

	@Override
	public void setDisplayName(String name)
	{
		myDataSource.setName(name);
	}

	@Override
	public EditableDataSource getEditableObject()
	{
		return null;
	}

	@Override
	public String getBannerSlogan()
	{
		return null;
	}

	@Override
	public JComponent createOptionsPanel()
	{
		return new JPanel();
	}
}
