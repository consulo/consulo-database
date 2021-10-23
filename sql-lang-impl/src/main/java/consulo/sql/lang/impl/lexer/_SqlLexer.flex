package consulo.sql.lang.impl.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import consulo.sql.lang.impl.psi.SqlTokenType;

@SuppressWarnings({"ALL"})
%%

%unicode
%public
%class _SqlLexer
%extends LexerBase
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

WHITE_SPACE_CHAR=[\ \r\t\f\n]

IDENTIFIER=[:jletter:] [:jletterdigit:]*

ESCAPE_SEQUENCE=\\[^\r\n]

SINGLE_QUOTED_LITERAL="'"([^\\\'\r\n]|{ESCAPE_SEQUENCE})*("'"|\\)?

DIGIT=[0-9]

NUMBER={DIGIT}*

%%

<YYINITIAL> {WHITE_SPACE_CHAR}+ { return TokenType.WHITE_SPACE; }

<YYINITIAL> {IDENTIFIER} { return SqlTokenType.IDENTIFIER; }

<YYINITIAL> {SINGLE_QUOTED_LITERAL} { return SqlTokenType.SINGLE_QUOTED_LITERAL; }

<YYINITIAL> {NUMBER} { return SqlTokenType.NUMBER; }

<YYINITIAL> "(" { return SqlTokenType.LPAR; }

<YYINITIAL> ")" { return SqlTokenType.RPAR; }

<YYINITIAL> "," { return SqlTokenType.COMMA; }

<YYINITIAL>  . { return TokenType.BAD_CHARACTER; }