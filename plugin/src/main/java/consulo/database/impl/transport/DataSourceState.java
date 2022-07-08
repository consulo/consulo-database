package consulo.database.impl.transport;

import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.util.xml.serializer.XmlSerializer;
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

	private final int myVersion;

	public DataSourceState(int version, Element xmlState, Object state)
	{
		myVersion = version;
		myState = state;
		myXmlState = xmlState == null ? null : xmlState.detach();
	}

	public int getVersion()
	{
		return myVersion;
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
