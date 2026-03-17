/**
 * @author VISTALL
 * @since 08-Jul-22
 */
module consulo.database.sql.lang.impl {
    requires consulo.database.sql.lang.api;

    requires consulo.language.editor.api;
    requires consulo.language.impl;

    exports consulo.sql.lang.impl;
    exports consulo.sql.lang.impl.psi;
    exports consulo.sql.lang.impl.lexer;
    exports consulo.sql.lang.impl.version.sql92;
}