package consulo.database.mysql;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.UnnamedConfigurable;
import consulo.database.datasource.jdbc.configurable.JdbcConfigurable;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.EditableDataSource;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class MysqJdbcDataSourceProvider extends JdbcDataSourceProvider
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

	@Nonnull
	@Override
	public UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource)
	{
		return new JdbcConfigurable((EditableDataSource) dataSource);
	}

	@Override
	public void fillDrivers(Map<String, String> map)
	{
		map.put("mysql-connector-java-8.0.21.jar", "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.21/mysql-connector-java-8.0.21.jar");
	}
}
