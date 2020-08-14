package consulo.database.mongo;

import consulo.database.datasource.json.JsonDataSourceProvider;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

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
}
