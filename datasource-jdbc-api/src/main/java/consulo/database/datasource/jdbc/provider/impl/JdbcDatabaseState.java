package consulo.database.datasource.jdbc.provider.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class JdbcDatabaseState implements PersistentStateComponent<JdbcDatabaseState>
{
	private JdbcTablesState myTablesState;

	private String myName;

	public JdbcDatabaseState()
	{
		myTablesState = new JdbcTablesState();
	}

	public JdbcDatabaseState(@Nonnull String name, @Nonnull JdbcTablesState tables)
	{
		myName =  name;
		myTablesState = tables;
	}

	public String getName()
	{
		return myName;
	}

	public void setName(String name)
	{
		myName = name;
	}

	public JdbcTablesState getTablesState()
	{
		return myTablesState;
	}

	public void setTablesState(JdbcTablesState tablesState)
	{
		myTablesState = tablesState;
	}

	@Nullable
	@Override
	public JdbcDatabaseState getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcDatabaseState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
