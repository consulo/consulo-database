package consulo.database.datasource.transport;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jdom.Element;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class FakeResult implements PersistentStateComponent<Element>
{
	@Nullable
	@Override
	public Element getState()
	{
		return new Element("state");
	}

	@Override
	public void loadState(Element state)
	{

	}
}
