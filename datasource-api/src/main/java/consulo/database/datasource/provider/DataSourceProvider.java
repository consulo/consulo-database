package consulo.database.datasource.provider;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.options.UnnamedConfigurable;
import consulo.database.datasource.model.DataSource;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-12
 */
public interface DataSourceProvider
{
	ExtensionPointName<DataSourceProvider> EP_NAME = ExtensionPointName.create("consulo.database.provider");

	@Nonnull
	String getId();

	@Nonnull
	LocalizeValue getName();

	@Nonnull
	Image getIcon();

	@Nonnull
	UnnamedConfigurable createConfigurable(@Nonnull DataSource dataSource);
}
