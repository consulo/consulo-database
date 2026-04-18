/**
 * @author VISTALL
 * @since 08-Jul-22
 */
module consulo.database
{
	requires consulo.database.datasource.api;
	requires consulo.database.datasource.jdbc.api;
	requires consulo.database.sql.lang.api;

	requires consulo.application.api;
	requires consulo.code.editor.api;
	requires consulo.component.api;
	requires consulo.configurable.api;
	requires consulo.credential.storage.api;
	requires consulo.datacontext.api;
	requires consulo.disposer.api;
	requires consulo.file.editor.api;
	requires consulo.language.api;
	requires consulo.localize.api;
	requires consulo.logging.api;
	requires consulo.base.icon.library;
	requires consulo.project.api;
	requires consulo.project.ui.api;
	requires consulo.project.ui.view.api;
	requires consulo.proxy;
	requires consulo.ui.api;
	requires consulo.ui.ex.api;
	requires consulo.ui.ex.awt.api;
	requires consulo.util.collection;
	requires consulo.util.concurrent;
	requires consulo.util.dataholder;
	requires consulo.util.lang;
	requires consulo.util.xml.serializer;
	requires consulo.virtual.file.system.api;

	opens consulo.database.impl.store to consulo.proxy;

	// TODO remove this in future
	requires java.desktop;
}
