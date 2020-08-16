package consulo.database.mongo;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import consulo.database.datasource.json.JsonDataSourceProvider;
import consulo.database.datasource.model.DataSource;
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
 * @since 2020-08-14
 */
public class MongoDbDataSourceProvider implements JsonDataSourceProvider
{
	@Nonnull
	@Override
	public String getId()
	{
		return "mongodb";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("MongoDB");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return MongoIcons.MongoDb;
	}

	@Nonnull
	@Override
	public UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource)
	{
		return new UnnamedConfigurable()
		{
			@RequiredUIAccess
			@Nullable
			@Override
			public Component createUIComponent()
			{
				return DockLayout.create().center(Label.create("mongo db test"));
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
