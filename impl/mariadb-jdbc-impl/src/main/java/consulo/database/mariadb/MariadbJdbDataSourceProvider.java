package consulo.database.mariadb;

import com.intellij.openapi.options.UnnamedConfigurable;
import consulo.database.datasource.jdbc.configurable.JdbcConfigurable;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSource;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class MariadbJdbDataSourceProvider extends JdbcDataSourceProvider
{
	@Nonnull
	@Override
	public String getId()
	{
		return "mariadb";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("MariaDB");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return MariadbIcons.Mariadb;
	}

	@Nonnull
	@Override
	public UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource)
	{
		return new JdbcConfigurable((EditableDataSource) dataSource);
	}
}