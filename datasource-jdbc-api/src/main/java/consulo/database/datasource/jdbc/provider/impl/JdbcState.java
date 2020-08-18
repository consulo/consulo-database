package consulo.database.datasource.jdbc.provider.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class JdbcState implements PersistentStateComponent<JdbcState>
{
	private Map<String, JdbcDatabaseState> myDatabases = new TreeMap<>();

	@Nonnull
	public Map<String, JdbcDatabaseState> getDatabases()
	{
		return myDatabases;
	}

	public void addDatabase(@Nonnull String dbName, @Nonnull JdbcDatabaseState databaseState)
	{
		myDatabases.put(dbName, databaseState);
	}

	@Nullable
	@Override
	public JdbcState getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
