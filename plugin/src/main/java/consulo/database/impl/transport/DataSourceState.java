package consulo.database.impl.transport;

import com.intellij.util.xmlb.XmlSerializer;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
public class DataSourceState
{
	private Element myXmlState;

	private Object myState;

	public DataSourceState(Element xmlState, Object state)
	{
		myXmlState = xmlState == null ? null : xmlState.detach();
		myState = state;
	}

	@Nonnull
	public Element toXmlState()
	{
		if(myXmlState != null)
		{
			return myXmlState.clone();
		}

		return XmlSerializer.serialize(myState);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getObjectState(@Nonnull DataSource dataSource, @Nonnull DataSourceTransport dataSourceTransport)
	{
		if(myState != null)
		{
			return (T) myState;
		}

		myState = XmlSerializer.deserialize(myXmlState, dataSourceTransport.getStateClass());
		myXmlState = null;
		return (T) myState;
	}
}
