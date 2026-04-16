/**
 * @author VISTALL
 * @since 08-Jul-22
 */
module consulo.database.mysql.jdbc.impl {
    requires consulo.database.datasource.jdbc.api;
    requires consulo.configurable.api;
    requires consulo.language.api;

    requires consulo.database.sql.mysql.impl;
}