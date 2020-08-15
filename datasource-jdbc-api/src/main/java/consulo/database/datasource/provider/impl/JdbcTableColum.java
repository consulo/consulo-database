package consulo.database.datasource.provider.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class JdbcTableColum implements PersistentStateComponent<JdbcTableColum>
{
	private String myName;
	private String myType;

	private JdbcTableColum()
	{
	}

	public JdbcTableColum(String name, String type)
	{
		myName = name;
		myType = type;
	}

	public String getName()
	{
		return myName;
	}

	public void setType(String type)
	{
		myType = type;
	}

	public String getType()
	{
		return myType;
	}

	public void setName(String name)
	{
		myName = name;
	}

	@Nullable
	@Override
	public JdbcTableColum getState()
	{
		return this;
	}

	@Override
	public void loadState(JdbcTableColum state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
