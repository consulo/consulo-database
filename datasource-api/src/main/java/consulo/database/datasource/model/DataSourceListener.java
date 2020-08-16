package consulo.database.datasource.model;

import consulo.database.datasource.model.DataSourceEvent;

import java.util.EventListener;

/**
 * @author VISTALL
 * @since 2020-08-14
 */
public interface DataSourceListener extends EventListener
{
	void dataSourceEvent(DataSourceEvent event);
}
