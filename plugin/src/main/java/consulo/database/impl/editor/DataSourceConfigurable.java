package consulo.database.impl.editor;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.ui.NamedConfigurable;
import consulo.database.datasource.model.EditableDataSource;
import consulo.options.ConfigurableUIMigrationUtil;
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

	private UnnamedConfigurable myInnerConfigurable;

	public DataSourceConfigurable(EditableDataSource dataSource, Runnable treeUpdater)
	{
		super(true, treeUpdater);
		myDataSource = dataSource;
	}

	@RequiredUIAccess
	@Override
	public boolean isModified()
	{
		return myInnerConfigurable != null && myInnerConfigurable.isModified();
	}

	@RequiredUIAccess
	@Override
	public void apply() throws ConfigurationException
	{
		if(myInnerConfigurable != null)
		{
			myInnerConfigurable.apply();
		}
	}

	@RequiredUIAccess
	@Override
	public void reset()
	{
		// need create ui
		createComponent();
		
		if(myInnerConfigurable != null)
		{
			myInnerConfigurable.reset();
		}
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
	@RequiredUIAccess
	public JComponent createOptionsPanel()
	{
		if(myInnerConfigurable == null)
		{
			myInnerConfigurable = myDataSource.getProvider().createConfigurable(myDataSource);
		}
		return ConfigurableUIMigrationUtil.createComponent(myInnerConfigurable);
	}
}
