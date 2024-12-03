/**
 * @author VISTALL
 * @since 06-Jul-22
 */
module consulo.database.datasource.api {
	// TODO remove this dependency in future
	requires java.desktop;

	requires transitive consulo.ide.api;

	requires transitive jakarta.annotation;

	exports consulo.database.datasource;
	exports consulo.database.datasource.configurable;
	exports consulo.database.datasource.editor;
	exports consulo.database.datasource.model;
	exports consulo.database.datasource.provider;
	exports consulo.database.datasource.transport;
	exports consulo.database.datasource.transport.ui;
	exports consulo.database.datasource.ui;
	exports consulo.database.icon;
}