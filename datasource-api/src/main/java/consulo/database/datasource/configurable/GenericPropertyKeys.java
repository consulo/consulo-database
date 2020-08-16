package consulo.database.datasource.configurable;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface GenericPropertyKeys
{
	GenericPropertyKey<String> HOST = GenericPropertyKey.create("host", String.class, "locahost");

	GenericPropertyKey<Integer> PORT = GenericPropertyKey.create("port", Integer.class, 0);

	GenericPropertyKey<String> LOGIN = GenericPropertyKey.create("login", String.class);

	GenericPropertyKey<String> PASSWORD = GenericPropertyKey.create("password", String.class);

	GenericPropertyKey<String> DATABASE_NAME = GenericPropertyKey.create("database-name", String.class);
}
