package consulo.database.datasource.jdbc.provider.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class JdbcTableState implements PersistentStateComponent<JdbcTableState>
{
	private String myName;

	private List<JdbcTableColumState> myColumns = new ArrayList<>();

	public void setName(String name)
	{
		myName = name;
	}

	public String getName()
	{
		return myName;
	}

	public List<JdbcTableColumState> getColumns()
	{
		return myColumns;
	}

	@Nullable
	@Override
	public JdbcTableState getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcTableState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
