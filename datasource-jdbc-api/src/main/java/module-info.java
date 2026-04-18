/**
 * @author VISTALL
 * @since 08-Jul-22
 */
module consulo.database.datasource.jdbc.api
{
	requires transitive consulo.database.datasource.api;
	requires consulo.database.datasource.jdbc.rt.shared;

	requires consulo.application.api;
	requires consulo.component.api;
	requires consulo.container.api;
	requires consulo.configurable.api;
	requires consulo.disposer.api;
	requires consulo.ide.api;
	requires consulo.language.editor.ui.api;
	requires consulo.localize.api;
	requires consulo.logging.api;
	requires consulo.platform.api;
	requires consulo.process.api;
	requires consulo.project.api;
	requires consulo.project.ui.view.api;
	requires consulo.ui.api;
	requires consulo.ui.ex.api;
	requires consulo.ui.ex.awt.api;
	requires consulo.util.collection;
	requires consulo.util.concurrent;
	requires consulo.util.dataholder;
	requires consulo.util.io;
	requires consulo.util.lang;
	requires consulo.util.xml.serializer;

	requires org.apache.thrift;

	requires java.sql;

	requires org.slf4j;

	// TODO [VISTALL] remove this dep in future
	requires java.desktop;

	exports consulo.database.datasource.jdbc.configurable;
	exports consulo.database.datasource.jdbc.provider;
	exports consulo.database.datasource.jdbc.provider.impl;
	exports consulo.database.datasource.jdbc.transport;
	exports consulo.database.datasource.jdbc.transport.columnInfo;
	exports consulo.database.datasource.jdbc.transport.ui;
	exports consulo.database.datasource.jdbc.ui;
	exports consulo.database.datasource.jdbc.ui.tree;

	opens consulo.database.datasource.jdbc.provider.impl to consulo.util.xml.serializer;
}
