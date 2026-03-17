/**
 * @author VISTALL
 * @since 08-Jul-22
 */
module consulo.database.sql.lang.api
{
    requires transitive consulo.language.api;

    requires transitive consulo.localize.api;

    exports consulo.sql.lang.api;
    exports consulo.sql.lang.api.icon;
    exports consulo.sql.lang.api.psi;
    exports consulo.sql.lang.localize;
}