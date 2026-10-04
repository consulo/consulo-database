/**
 * @author VISTALL
 * @since 06-Jul-22
 */
module consulo.database.datasource.api {
	requires transitive consulo.application.api;
	requires transitive consulo.component.api;
	requires transitive consulo.project.api;
	requires transitive consulo.disposer.api;
	requires transitive consulo.localize.api;
	requires transitive consulo.ui.api;
	requires transitive consulo.ui.ex.api;

	requires consulo.configurable.api;
	requires consulo.language.api;
	requires consulo.project.ui.view.api;
	requires consulo.util.concurrent;
	requires consulo.util.dataholder;

	requires transitive jakarta.annotation;

	exports consulo.database.datasource;
	exports consulo.database.datasource.configurable;
	exports consulo.database.datasource.driver;
	exports consulo.database.datasource.editor;
	exports consulo.database.datasource.localize;
	exports consulo.database.datasource.model;
	exports consulo.database.datasource.provider;
	exports consulo.database.datasource.transport;
	exports consulo.database.datasource.transport.ui;
	exports consulo.database.datasource.ui;
	exports consulo.database.icon;
}
