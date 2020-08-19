package consulo.database.postgresql;

import com.intellij.icons.AllIcons;
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
 * @since 2020-08-19
 */
public class PostgresqlJdbcDataSourceProvider extends JdbcDataSourceProvider
{
	@Nonnull
	@Override
	public String getId()
	{
		return "postgresql";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("PostgreSQL");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return AllIcons.Providers.Postgresql;
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
		propertiesHolder.set(GenericPropertyKeys.PORT, 5432);
		propertiesHolder.set(GenericPropertyKeys.LOGIN, "postgres");
		propertiesHolder.set(GenericPropertyKeys.PASSWORD, "postgres");
	}

	@Override
	public void fillDrivers(Map<String, String> map)
	{
		map.put("postgresql-42.2.15.jar", "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.15/postgresql-42.2.15.jar");
	}
}
