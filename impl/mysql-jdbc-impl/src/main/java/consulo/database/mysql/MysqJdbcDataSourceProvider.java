package consulo.database.mysql;

import com.intellij.icons.AllIcons;
import consulo.database.datasource.provider.JdbcDataSourceProvider;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class MysqJdbcDataSourceProvider implements JdbcDataSourceProvider
{
	@Nonnull
	@Override
	public String getId()
	{
		return "mysql";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("MySQL");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return AllIcons.Providers.Mysql;
	}
}
