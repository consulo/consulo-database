package consulo.database.datasource;

import java.util.EventObject;

/**
 * @author VISTALL
 * @since 2020-08-15
 */
public class DataSourceEvent extends EventObject
{
	public enum Action
	{
		ADD,
		CHANGE,
		REMOVE
	}

	private final Action myAction;
	private final DataSource myDataSource;

	public DataSourceEvent(DataSourceManager source, Action action, DataSource dataSource)
	{
		super(source);
		myAction = action;
		myDataSource = dataSource;
	}

	public DataSource getDataSource()
	{
		return myDataSource;
	}

	public Action getAction()
	{
		return myAction;
	}

	@Override
	public DataSourceManager getSource()
	{
		return (DataSourceManager) super.getSource();
	}
}
