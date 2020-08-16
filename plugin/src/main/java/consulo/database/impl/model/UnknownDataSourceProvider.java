package consulo.database.impl.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.util.text.StringUtil;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.localize.LocalizeValue;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.ui.layout.DockLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class UnknownDataSourceProvider implements DataSourceProvider
{
	private final String myId;

	public UnknownDataSourceProvider(String id)
	{
		myId = id;
	}

	@Nonnull
	@Override
	public String getId()
	{
		return myId;
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of(StringUtil.capitalize(myId));
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return AllIcons.Actions.Help;
	}

	@Override
	public UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource)
	{
		return new UnnamedConfigurable()
		{
			@RequiredUIAccess
			@Override
			public Component createUIComponent()
			{
				return DockLayout.create().center(Label.create("Unknown datasource provider"));
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
		};
	}
}
