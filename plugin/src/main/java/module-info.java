/**
 * @author VISTALL
 * @since 08-Jul-22
 */
module consulo.database
{
	requires consulo.ide.api;
	requires consulo.database.datasource.api;
	requires consulo.database.sql.lang.api;
	requires consulo.database.datasource.jdbc.api;

	opens consulo.database.impl.store to consulo.proxy;

	// TODO remove this in future
	requires java.desktop;
}