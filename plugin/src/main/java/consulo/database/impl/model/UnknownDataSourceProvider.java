package consulo.database.impl.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class UnknownDataSourceProvider implements DataSourceProvider
{
	private final String myId;

	public UnknownDataSourceProvider(String id)
	{
		myId = id;
	}

	@Nonnull
	@Override
	public String getId()
	{
		return myId;
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of(StringUtil.capitalize(myId));
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return AllIcons.Actions.Help;
	}
}
