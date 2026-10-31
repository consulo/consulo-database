/**
 * @author VISTALL
 * @since 2026-10-31
 */
module consulo.database.sqlite.jdbc.impl {
    requires consulo.database.datasource.jdbc.api;

    requires consulo.configurable.api;
    requires consulo.disposer.api;
    requires consulo.file.chooser.api;
    requires consulo.language.api;
    requires consulo.localize.api;
    requires consulo.ui.api;
}
