/**
 * @author VISTALL
 * @since 08-Jul-22
 */
module consulo.database.datasource.jdbc.api
{
	requires transitive consulo.database.datasource.api;

	requires consulo.database.datasource.jdbc.rt.shared;

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