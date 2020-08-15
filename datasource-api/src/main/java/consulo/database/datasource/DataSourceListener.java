package consulo.database.datasource;

import java.util.EventListener;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public interface DataSourceListener extends EventListener
{
	void dataSourceEvent(DataSourceEvent event);
}
