package consulo.database.datasource.configurable;

import consulo.util.dataholder.Key;
import consulo.util.dataholder.KeyWithDefaultValue;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public interface GenericPropertyKeys
{
	Key<String> HOST = KeyWithDefaultValue.create("host", "locahost");

	Key<String> LOGIN = Key.create("login");

	Key<String> PASSWORD = Key.create("password");

	Key<String> DATABASE_NAME = Key.create("database-name");
}
