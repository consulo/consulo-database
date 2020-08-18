package consulo.database.mariadb;

import com.intellij.openapi.options.UnnamedConfigurable;
import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.configurable.GenericPropertyKeys;
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

	@Override
	public void fillDefaultProperties(@Nonnull EditablePropertiesHolder propertiesHolder)
	{
		propertiesHolder.set(GenericPropertyKeys.PORT, 3306);
		propertiesHolder.set(GenericPropertyKeys.LOGIN, "root");
		propertiesHolder.set(GenericPropertyKeys.PASSWORD, "root");
	}

	@Override
	public void fillDrivers(Map<String, String> map)
	{
		map.put("mariadb-java-client-2.6.2.jar", "https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.6.2/mariadb-java-client-2.6.2.jar");
	}
}