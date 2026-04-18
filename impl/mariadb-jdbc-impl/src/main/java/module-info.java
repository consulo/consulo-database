/**
 * @author VISTALL
 * @since 08-Jul-22
 */
module consulo.database.mariadb.jdbc.impl {
    requires consulo.database.datasource.jdbc.api;
    requires consulo.database.sql.mariadb.impl;

    requires consulo.configurable.api;
    requires consulo.language.api;
    requires consulo.localize.api;
    requires consulo.ui.api;
}
