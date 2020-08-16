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
public class JdbcTable implements PersistentStateComponent<JdbcTable>
{
	private String myName;

	private List<JdbcTableColum> myColumns = new ArrayList<>();

	public void setName(String name)
	{
		myName = name;
	}

	public String getName()
	{
		return myName;
	}

	public List<JdbcTableColum> getColumns()
	{
		return myColumns;
	}

	@Nullable
	@Override
	public JdbcTable getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcTable state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
