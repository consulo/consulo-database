package consulo.database.datasource.jdbc.configurable;

import consulo.database.datasource.configurable.EditablePropertiesHolder;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.model.EditableDataSource;
import consulo.options.SimpleConfigurableByProperties;
import consulo.ui.Component;
import consulo.ui.IntBox;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.shared.border.BorderStyle;
import consulo.ui.util.FormBuilder;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class JdbcConfigurable extends SimpleConfigurableByProperties
{
	private final EditableDataSource myDataSource;

	public JdbcConfigurable(EditableDataSource dataSource)
	{
		myDataSource = dataSource;
	}

	@RequiredUIAccess
	@Nonnull
	@Override
	protected Component createLayout(PropertyBuilder propertyBuilder)
	{
		VerticalLayout layout = VerticalLayout.create();
		layout.addBorders(BorderStyle.EMPTY, null, 10);

		FormBuilder builder = new FormBuilder();

		EditablePropertiesHolder properties = myDataSource.getProperties();

		TextBox hostBox = TextBox.create();
		builder.addLabeled("Host", hostBox);
		propertyBuilder.add(hostBox, () -> properties.get(GenericPropertyKeys.HOST), it -> properties.set(GenericPropertyKeys.HOST, it));

		IntBox portBox = IntBox.create();
		builder.addLabeled("Port", portBox);
		propertyBuilder.add(portBox, () -> properties.get(GenericPropertyKeys.PORT), it -> properties.set(GenericPropertyKeys.PORT, it));

		TextBox loginBox = TextBox.create();
		builder.addLabeled("Login", loginBox);
		propertyBuilder.add(loginBox, () -> properties.get(GenericPropertyKeys.LOGIN), it -> properties.set(GenericPropertyKeys.LOGIN, it));

		TextBox passwordBox = TextBox.create();
		builder.addLabeled("Password", passwordBox);
		propertyBuilder.add(passwordBox, () -> properties.get(GenericPropertyKeys.PASSWORD), it -> properties.set(GenericPropertyKeys.PASSWORD, it));

		TextBox databaseNameBox = TextBox.create();
		builder.addLabeled("Database Name", databaseNameBox);
		propertyBuilder.add(databaseNameBox, () -> properties.get(GenericPropertyKeys.DATABASE_NAME), it -> properties.set(GenericPropertyKeys.DATABASE_NAME, it));

		layout.add(builder.build());
		return layout;
	}
}
