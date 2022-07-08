/*
 * Copyright 2013-2021 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.sql.lang.impl.psi;

import consulo.language.ast.IElementType;
import consulo.sql.lang.api.SqlLanguage;

/**
 * @author VISTALL
 * @since 22/10/2021
 */
public interface SqlKeywordTokenTypes
{
	IElementType ABSOLUTE_KEYWORD = new SqlKeywordElementType("ABSOLUTE", SqlLanguage.INSTANCE);
	IElementType ACTION_KEYWORD = new SqlKeywordElementType("ACTION", SqlLanguage.INSTANCE);
	IElementType ADD_KEYWORD = new SqlKeywordElementType("ADD", SqlLanguage.INSTANCE);
	IElementType ALL_KEYWORD = new SqlKeywordElementType("ALL", SqlLanguage.INSTANCE);
	IElementType ALLOCATE_KEYWORD = new SqlKeywordElementType("ALLOCATE", SqlLanguage.INSTANCE);
	IElementType ALTER_KEYWORD = new SqlKeywordElementType("ALTER", SqlLanguage.INSTANCE);
	IElementType AND_KEYWORD = new SqlKeywordElementType("AND", SqlLanguage.INSTANCE);
	IElementType ANY_KEYWORD = new SqlKeywordElementType("ANY", SqlLanguage.INSTANCE);
	IElementType ARE_KEYWORD = new SqlKeywordElementType("ARE", SqlLanguage.INSTANCE);
	IElementType AS_KEYWORD = new SqlKeywordElementType("AS", SqlLanguage.INSTANCE);
	IElementType ASC_KEYWORD = new SqlKeywordElementType("ASC", SqlLanguage.INSTANCE);
	IElementType ASSERTION_KEYWORD = new SqlKeywordElementType("ASSERTION", SqlLanguage.INSTANCE);
	IElementType AT_KEYWORD = new SqlKeywordElementType("AT", SqlLanguage.INSTANCE);
	IElementType AUTHORIZATION_KEYWORD = new SqlKeywordElementType("AUTHORIZATION", SqlLanguage.INSTANCE);
	IElementType AVG_KEYWORD = new SqlKeywordElementType("AVG", SqlLanguage.INSTANCE);
	IElementType BEGIN_KEYWORD = new SqlKeywordElementType("BEGIN", SqlLanguage.INSTANCE);
	IElementType BETWEEN_KEYWORD = new SqlKeywordElementType("BETWEEN", SqlLanguage.INSTANCE);
	IElementType BIT_KEYWORD = new SqlKeywordElementType("BIT", SqlLanguage.INSTANCE);
	IElementType BIT_LENGTH_KEYWORD = new SqlKeywordElementType("BIT_LENGTH", SqlLanguage.INSTANCE);
	IElementType BOTH_KEYWORD = new SqlKeywordElementType("BOTH", SqlLanguage.INSTANCE);
	IElementType BY_KEYWORD = new SqlKeywordElementType("BY", SqlLanguage.INSTANCE);
	IElementType CALL_KEYWORD = new SqlKeywordElementType("CALL", SqlLanguage.INSTANCE);
	IElementType CASCADE_KEYWORD = new SqlKeywordElementType("CASCADE", SqlLanguage.INSTANCE);
	IElementType CASCADED_KEYWORD = new SqlKeywordElementType("CASCADED", SqlLanguage.INSTANCE);
	IElementType CASE_KEYWORD = new SqlKeywordElementType("CASE", SqlLanguage.INSTANCE);
	IElementType CAST_KEYWORD = new SqlKeywordElementType("CAST", SqlLanguage.INSTANCE);
	IElementType CATALOG_KEYWORD = new SqlKeywordElementType("CATALOG", SqlLanguage.INSTANCE);
	IElementType CHAR_KEYWORD = new SqlKeywordElementType("CHAR", SqlLanguage.INSTANCE);
	IElementType CHAR_LENGTH_KEYWORD = new SqlKeywordElementType("CHAR_LENGTH", SqlLanguage.INSTANCE);
	IElementType CHARACTER_KEYWORD = new SqlKeywordElementType("CHARACTER", SqlLanguage.INSTANCE);
	IElementType CHARACTER_LENGTH_KEYWORD = new SqlKeywordElementType("CHARACTER_LENGTH", SqlLanguage.INSTANCE);
	IElementType CHECK_KEYWORD = new SqlKeywordElementType("CHECK", SqlLanguage.INSTANCE);
	IElementType CLOSE_KEYWORD = new SqlKeywordElementType("CLOSE", SqlLanguage.INSTANCE);
	IElementType COALESCE_KEYWORD = new SqlKeywordElementType("COALESCE", SqlLanguage.INSTANCE);
	IElementType COLLATE_KEYWORD = new SqlKeywordElementType("COLLATE", SqlLanguage.INSTANCE);
	IElementType COLLATION_KEYWORD = new SqlKeywordElementType("COLLATION", SqlLanguage.INSTANCE);
	IElementType COLUMN_KEYWORD = new SqlKeywordElementType("COLUMN", SqlLanguage.INSTANCE);
	IElementType COMMIT_KEYWORD = new SqlKeywordElementType("COMMIT", SqlLanguage.INSTANCE);
	IElementType CONDITION_KEYWORD = new SqlKeywordElementType("CONDITION", SqlLanguage.INSTANCE);
	IElementType CONNECT_KEYWORD = new SqlKeywordElementType("CONNECT", SqlLanguage.INSTANCE);
	IElementType CONNECTION_KEYWORD = new SqlKeywordElementType("CONNECTION", SqlLanguage.INSTANCE);
	IElementType CONSTRAINT_KEYWORD = new SqlKeywordElementType("CONSTRAINT", SqlLanguage.INSTANCE);
	IElementType CONSTRAINTS_KEYWORD = new SqlKeywordElementType("CONSTRAINTS", SqlLanguage.INSTANCE);
	IElementType CONTAINS_KEYWORD = new SqlKeywordElementType("CONTAINS", SqlLanguage.INSTANCE);
	IElementType CONTINUE_KEYWORD = new SqlKeywordElementType("CONTINUE", SqlLanguage.INSTANCE);
	IElementType CONVERT_KEYWORD = new SqlKeywordElementType("CONVERT", SqlLanguage.INSTANCE);
	IElementType CORRESPONDING_KEYWORD = new SqlKeywordElementType("CORRESPONDING", SqlLanguage.INSTANCE);
	IElementType COUNT_KEYWORD = new SqlKeywordElementType("COUNT", SqlLanguage.INSTANCE);
	IElementType CREATE_KEYWORD = new SqlKeywordElementType("CREATE", SqlLanguage.INSTANCE);
	IElementType CROSS_KEYWORD = new SqlKeywordElementType("CROSS", SqlLanguage.INSTANCE);
	IElementType CURRENT_KEYWORD = new SqlKeywordElementType("CURRENT", SqlLanguage.INSTANCE);
	IElementType CURRENT_DATE_KEYWORD = new SqlKeywordElementType("CURRENT_DATE", SqlLanguage.INSTANCE);
	IElementType CURRENT_PATH_KEYWORD = new SqlKeywordElementType("CURRENT_PATH", SqlLanguage.INSTANCE);
	IElementType CURRENT_TIME_KEYWORD = new SqlKeywordElementType("CURRENT_TIME", SqlLanguage.INSTANCE);
	IElementType CURRENT_TIMESTAMP_KEYWORD = new SqlKeywordElementType("CURRENT_TIMESTAMP", SqlLanguage.INSTANCE);
	IElementType CURRENT_USER_KEYWORD = new SqlKeywordElementType("CURRENT_USER", SqlLanguage.INSTANCE);
	IElementType CURSOR_KEYWORD = new SqlKeywordElementType("CURSOR", SqlLanguage.INSTANCE);
	IElementType DATE_KEYWORD = new SqlKeywordElementType("DATE", SqlLanguage.INSTANCE);
	IElementType DAY_KEYWORD = new SqlKeywordElementType("DAY", SqlLanguage.INSTANCE);
	IElementType DEALLOCATE_KEYWORD = new SqlKeywordElementType("DEALLOCATE", SqlLanguage.INSTANCE);
	IElementType DEC_KEYWORD = new SqlKeywordElementType("DEC", SqlLanguage.INSTANCE);
	IElementType DECIMAL_KEYWORD = new SqlKeywordElementType("DECIMAL", SqlLanguage.INSTANCE);
	IElementType DECLARE_KEYWORD = new SqlKeywordElementType("DECLARE", SqlLanguage.INSTANCE);
	IElementType DEFAULT_KEYWORD = new SqlKeywordElementType("DEFAULT", SqlLanguage.INSTANCE);
	IElementType DEFERRABLE_KEYWORD = new SqlKeywordElementType("DEFERRABLE", SqlLanguage.INSTANCE);
	IElementType DEFERRED_KEYWORD = new SqlKeywordElementType("DEFERRED", SqlLanguage.INSTANCE);
	IElementType DELETE_KEYWORD = new SqlKeywordElementType("DELETE", SqlLanguage.INSTANCE);
	IElementType DESC_KEYWORD = new SqlKeywordElementType("DESC", SqlLanguage.INSTANCE);
	IElementType DESCRIBE_KEYWORD = new SqlKeywordElementType("DESCRIBE", SqlLanguage.INSTANCE);
	IElementType DESCRIPTOR_KEYWORD = new SqlKeywordElementType("DESCRIPTOR", SqlLanguage.INSTANCE);
	IElementType DETERMINISTIC_KEYWORD = new SqlKeywordElementType("DETERMINISTIC", SqlLanguage.INSTANCE);
	IElementType DIAGNOSTICS_KEYWORD = new SqlKeywordElementType("DIAGNOSTICS", SqlLanguage.INSTANCE);
	IElementType DISCONNECT_KEYWORD = new SqlKeywordElementType("DISCONNECT", SqlLanguage.INSTANCE);
	IElementType DISTINCT_KEYWORD = new SqlKeywordElementType("DISTINCT", SqlLanguage.INSTANCE);
	IElementType DO_KEYWORD = new SqlKeywordElementType("DO", SqlLanguage.INSTANCE);
	IElementType DOMAIN_KEYWORD = new SqlKeywordElementType("DOMAIN", SqlLanguage.INSTANCE);
	IElementType DOUBLE_KEYWORD = new SqlKeywordElementType("DOUBLE", SqlLanguage.INSTANCE);
	IElementType DROP_KEYWORD = new SqlKeywordElementType("DROP", SqlLanguage.INSTANCE);
	IElementType ELSE_KEYWORD = new SqlKeywordElementType("ELSE", SqlLanguage.INSTANCE);
	IElementType ELSEIF_KEYWORD = new SqlKeywordElementType("ELSEIF", SqlLanguage.INSTANCE);
	IElementType END_KEYWORD = new SqlKeywordElementType("END", SqlLanguage.INSTANCE);
	IElementType ESCAPE_KEYWORD = new SqlKeywordElementType("ESCAPE", SqlLanguage.INSTANCE);
	IElementType EXCEPT_KEYWORD = new SqlKeywordElementType("EXCEPT", SqlLanguage.INSTANCE);
	IElementType EXCEPTION_KEYWORD = new SqlKeywordElementType("EXCEPTION", SqlLanguage.INSTANCE);
	IElementType EXEC_KEYWORD = new SqlKeywordElementType("EXEC", SqlLanguage.INSTANCE);
	IElementType EXECUTE_KEYWORD = new SqlKeywordElementType("EXECUTE", SqlLanguage.INSTANCE);
	IElementType EXISTS_KEYWORD = new SqlKeywordElementType("EXISTS", SqlLanguage.INSTANCE);
	IElementType EXIT_KEYWORD = new SqlKeywordElementType("EXIT", SqlLanguage.INSTANCE);
	IElementType EXTERNAL_KEYWORD = new SqlKeywordElementType("EXTERNAL", SqlLanguage.INSTANCE);
	IElementType EXTRACT_KEYWORD = new SqlKeywordElementType("EXTRACT", SqlLanguage.INSTANCE);
	IElementType FALSE_KEYWORD = new SqlKeywordElementType("FALSE", SqlLanguage.INSTANCE);
	IElementType FETCH_KEYWORD = new SqlKeywordElementType("FETCH", SqlLanguage.INSTANCE);
	IElementType FIRST_KEYWORD = new SqlKeywordElementType("FIRST", SqlLanguage.INSTANCE);
	IElementType FLOAT_KEYWORD = new SqlKeywordElementType("FLOAT", SqlLanguage.INSTANCE);
	IElementType FOR_KEYWORD = new SqlKeywordElementType("FOR", SqlLanguage.INSTANCE);
	IElementType FOREIGN_KEYWORD = new SqlKeywordElementType("FOREIGN", SqlLanguage.INSTANCE);
	IElementType FOUND_KEYWORD = new SqlKeywordElementType("FOUND", SqlLanguage.INSTANCE);
	IElementType FROM_KEYWORD = new SqlKeywordElementType("FROM", SqlLanguage.INSTANCE);
	IElementType FULL_KEYWORD = new SqlKeywordElementType("FULL", SqlLanguage.INSTANCE);
	IElementType FUNCTION_KEYWORD = new SqlKeywordElementType("FUNCTION", SqlLanguage.INSTANCE);
	IElementType GET_KEYWORD = new SqlKeywordElementType("GET", SqlLanguage.INSTANCE);
	IElementType GLOBAL_KEYWORD = new SqlKeywordElementType("GLOBAL", SqlLanguage.INSTANCE);
	IElementType GO_KEYWORD = new SqlKeywordElementType("GO", SqlLanguage.INSTANCE);
	IElementType GOTO_KEYWORD = new SqlKeywordElementType("GOTO", SqlLanguage.INSTANCE);
	IElementType GRANT_KEYWORD = new SqlKeywordElementType("GRANT", SqlLanguage.INSTANCE);
	IElementType GROUP_KEYWORD = new SqlKeywordElementType("GROUP", SqlLanguage.INSTANCE);
	IElementType HANDLER_KEYWORD = new SqlKeywordElementType("HANDLER", SqlLanguage.INSTANCE);
	IElementType HAVING_KEYWORD = new SqlKeywordElementType("HAVING", SqlLanguage.INSTANCE);
	IElementType HOUR_KEYWORD = new SqlKeywordElementType("HOUR", SqlLanguage.INSTANCE);
	IElementType IDENTITY_KEYWORD = new SqlKeywordElementType("IDENTITY", SqlLanguage.INSTANCE);
	IElementType IF_KEYWORD = new SqlKeywordElementType("IF", SqlLanguage.INSTANCE);
	IElementType IMMEDIATE_KEYWORD = new SqlKeywordElementType("IMMEDIATE", SqlLanguage.INSTANCE);
	IElementType IN_KEYWORD = new SqlKeywordElementType("IN", SqlLanguage.INSTANCE);
	IElementType INDICATOR_KEYWORD = new SqlKeywordElementType("INDICATOR", SqlLanguage.INSTANCE);
	IElementType INITIALLY_KEYWORD = new SqlKeywordElementType("INITIALLY", SqlLanguage.INSTANCE);
	IElementType INNER_KEYWORD = new SqlKeywordElementType("INNER", SqlLanguage.INSTANCE);
	IElementType INOUT_KEYWORD = new SqlKeywordElementType("INOUT", SqlLanguage.INSTANCE);
	IElementType INPUT_KEYWORD = new SqlKeywordElementType("INPUT", SqlLanguage.INSTANCE);
	IElementType INSENSITIVE_KEYWORD = new SqlKeywordElementType("INSENSITIVE", SqlLanguage.INSTANCE);
	IElementType INSERT_KEYWORD = new SqlKeywordElementType("INSERT", SqlLanguage.INSTANCE);
	IElementType INT_KEYWORD = new SqlKeywordElementType("INT", SqlLanguage.INSTANCE);
	IElementType INTEGER_KEYWORD = new SqlKeywordElementType("INTEGER", SqlLanguage.INSTANCE);
	IElementType INTERSECT_KEYWORD = new SqlKeywordElementType("INTERSECT", SqlLanguage.INSTANCE);
	IElementType INTERVAL_KEYWORD = new SqlKeywordElementType("INTERVAL", SqlLanguage.INSTANCE);
	IElementType INTO_KEYWORD = new SqlKeywordElementType("INTO", SqlLanguage.INSTANCE);
	IElementType IS_KEYWORD = new SqlKeywordElementType("IS", SqlLanguage.INSTANCE);
	IElementType ISOLATION_KEYWORD = new SqlKeywordElementType("ISOLATION", SqlLanguage.INSTANCE);
	IElementType JOIN_KEYWORD = new SqlKeywordElementType("JOIN", SqlLanguage.INSTANCE);
	IElementType KEY_KEYWORD = new SqlKeywordElementType("KEY", SqlLanguage.INSTANCE);
	IElementType LANGUAGE_KEYWORD = new SqlKeywordElementType("LANGUAGE", SqlLanguage.INSTANCE);
	IElementType LAST_KEYWORD = new SqlKeywordElementType("LAST", SqlLanguage.INSTANCE);
	IElementType LEADING_KEYWORD = new SqlKeywordElementType("LEADING", SqlLanguage.INSTANCE);
	IElementType LEAVE_KEYWORD = new SqlKeywordElementType("LEAVE", SqlLanguage.INSTANCE);
	IElementType LEFT_KEYWORD = new SqlKeywordElementType("LEFT", SqlLanguage.INSTANCE);
	IElementType LEVEL_KEYWORD = new SqlKeywordElementType("LEVEL", SqlLanguage.INSTANCE);
	IElementType LIKE_KEYWORD = new SqlKeywordElementType("LIKE", SqlLanguage.INSTANCE);
	IElementType LOCAL_KEYWORD = new SqlKeywordElementType("LOCAL", SqlLanguage.INSTANCE);
	IElementType LOOP_KEYWORD = new SqlKeywordElementType("LOOP", SqlLanguage.INSTANCE);
	IElementType LOWER_KEYWORD = new SqlKeywordElementType("LOWER", SqlLanguage.INSTANCE);
	IElementType MATCH_KEYWORD = new SqlKeywordElementType("MATCH", SqlLanguage.INSTANCE);
	IElementType MAX_KEYWORD = new SqlKeywordElementType("MAX", SqlLanguage.INSTANCE);
	IElementType MIN_KEYWORD = new SqlKeywordElementType("MIN", SqlLanguage.INSTANCE);
	IElementType MINUTE_KEYWORD = new SqlKeywordElementType("MINUTE", SqlLanguage.INSTANCE);
	IElementType MODULE_KEYWORD = new SqlKeywordElementType("MODULE", SqlLanguage.INSTANCE);
	IElementType MONTH_KEYWORD = new SqlKeywordElementType("MONTH", SqlLanguage.INSTANCE);
	IElementType NAMES_KEYWORD = new SqlKeywordElementType("NAMES", SqlLanguage.INSTANCE);
	IElementType NATIONAL_KEYWORD = new SqlKeywordElementType("NATIONAL", SqlLanguage.INSTANCE);
	IElementType NATURAL_KEYWORD = new SqlKeywordElementType("NATURAL", SqlLanguage.INSTANCE);
	IElementType NCHAR_KEYWORD = new SqlKeywordElementType("NCHAR", SqlLanguage.INSTANCE);
	IElementType NEXT_KEYWORD = new SqlKeywordElementType("NEXT", SqlLanguage.INSTANCE);
	IElementType NO_KEYWORD = new SqlKeywordElementType("NO", SqlLanguage.INSTANCE);
	IElementType NOT_KEYWORD = new SqlKeywordElementType("NOT", SqlLanguage.INSTANCE);
	IElementType NULL_KEYWORD = new SqlKeywordElementType("NULL", SqlLanguage.INSTANCE);
	IElementType NULLIF_KEYWORD = new SqlKeywordElementType("NULLIF", SqlLanguage.INSTANCE);
	IElementType NUMERIC_KEYWORD = new SqlKeywordElementType("NUMERIC", SqlLanguage.INSTANCE);
	IElementType OCTET_LENGTH_KEYWORD = new SqlKeywordElementType("OCTET_LENGTH", SqlLanguage.INSTANCE);
	IElementType OF_KEYWORD = new SqlKeywordElementType("OF", SqlLanguage.INSTANCE);
	IElementType ON_KEYWORD = new SqlKeywordElementType("ON", SqlLanguage.INSTANCE);
	IElementType ONLY_KEYWORD = new SqlKeywordElementType("ONLY", SqlLanguage.INSTANCE);
	IElementType OPEN_KEYWORD = new SqlKeywordElementType("OPEN", SqlLanguage.INSTANCE);
	IElementType OPTION_KEYWORD = new SqlKeywordElementType("OPTION", SqlLanguage.INSTANCE);
	IElementType OR_KEYWORD = new SqlKeywordElementType("OR", SqlLanguage.INSTANCE);
	IElementType ORDER_KEYWORD = new SqlKeywordElementType("ORDER", SqlLanguage.INSTANCE);
	IElementType OUT_KEYWORD = new SqlKeywordElementType("OUT", SqlLanguage.INSTANCE);
	IElementType OUTER_KEYWORD = new SqlKeywordElementType("OUTER", SqlLanguage.INSTANCE);
	IElementType OUTPUT_KEYWORD = new SqlKeywordElementType("OUTPUT", SqlLanguage.INSTANCE);
	IElementType OVERLAPS_KEYWORD = new SqlKeywordElementType("OVERLAPS", SqlLanguage.INSTANCE);
	IElementType PAD_KEYWORD = new SqlKeywordElementType("PAD", SqlLanguage.INSTANCE);
	IElementType PARAMETER_KEYWORD = new SqlKeywordElementType("PARAMETER", SqlLanguage.INSTANCE);
	IElementType PARTIAL_KEYWORD = new SqlKeywordElementType("PARTIAL", SqlLanguage.INSTANCE);
	IElementType PATH_KEYWORD = new SqlKeywordElementType("PATH", SqlLanguage.INSTANCE);
	IElementType POSITION_KEYWORD = new SqlKeywordElementType("POSITION", SqlLanguage.INSTANCE);
	IElementType PRECISION_KEYWORD = new SqlKeywordElementType("PRECISION", SqlLanguage.INSTANCE);
	IElementType PREPARE_KEYWORD = new SqlKeywordElementType("PREPARE", SqlLanguage.INSTANCE);
	IElementType PRESERVE_KEYWORD = new SqlKeywordElementType("PRESERVE", SqlLanguage.INSTANCE);
	IElementType PRIMARY_KEYWORD = new SqlKeywordElementType("PRIMARY", SqlLanguage.INSTANCE);
	IElementType PRIOR_KEYWORD = new SqlKeywordElementType("PRIOR", SqlLanguage.INSTANCE);
	IElementType PRIVILEGES_KEYWORD = new SqlKeywordElementType("PRIVILEGES", SqlLanguage.INSTANCE);
	IElementType PROCEDURE_KEYWORD = new SqlKeywordElementType("PROCEDURE", SqlLanguage.INSTANCE);
	IElementType PUBLIC_KEYWORD = new SqlKeywordElementType("PUBLIC", SqlLanguage.INSTANCE);
	IElementType READ_KEYWORD = new SqlKeywordElementType("READ", SqlLanguage.INSTANCE);
	IElementType REAL_KEYWORD = new SqlKeywordElementType("REAL", SqlLanguage.INSTANCE);
	IElementType REFERENCES_KEYWORD = new SqlKeywordElementType("REFERENCES", SqlLanguage.INSTANCE);
	IElementType RELATIVE_KEYWORD = new SqlKeywordElementType("RELATIVE", SqlLanguage.INSTANCE);
	IElementType REPEAT_KEYWORD = new SqlKeywordElementType("REPEAT", SqlLanguage.INSTANCE);
	IElementType RESIGNAL_KEYWORD = new SqlKeywordElementType("RESIGNAL", SqlLanguage.INSTANCE);
	IElementType RESTRICT_KEYWORD = new SqlKeywordElementType("RESTRICT", SqlLanguage.INSTANCE);
	IElementType RETURN_KEYWORD = new SqlKeywordElementType("RETURN", SqlLanguage.INSTANCE);
	IElementType RETURNS_KEYWORD = new SqlKeywordElementType("RETURNS", SqlLanguage.INSTANCE);
	IElementType REVOKE_KEYWORD = new SqlKeywordElementType("REVOKE", SqlLanguage.INSTANCE);
	IElementType RIGHT_KEYWORD = new SqlKeywordElementType("RIGHT", SqlLanguage.INSTANCE);
	IElementType ROLLBACK_KEYWORD = new SqlKeywordElementType("ROLLBACK", SqlLanguage.INSTANCE);
	IElementType ROUTINE_KEYWORD = new SqlKeywordElementType("ROUTINE", SqlLanguage.INSTANCE);
	IElementType ROWS_KEYWORD = new SqlKeywordElementType("ROWS", SqlLanguage.INSTANCE);
	IElementType SCHEMA_KEYWORD = new SqlKeywordElementType("SCHEMA", SqlLanguage.INSTANCE);
	IElementType SCROLL_KEYWORD = new SqlKeywordElementType("SCROLL", SqlLanguage.INSTANCE);
	IElementType SECOND_KEYWORD = new SqlKeywordElementType("SECOND", SqlLanguage.INSTANCE);
	IElementType SECTION_KEYWORD = new SqlKeywordElementType("SECTION", SqlLanguage.INSTANCE);
	IElementType SELECT_KEYWORD = new SqlKeywordElementType("SELECT", SqlLanguage.INSTANCE);
	IElementType SESSION_KEYWORD = new SqlKeywordElementType("SESSION", SqlLanguage.INSTANCE);
	IElementType SESSION_USER_KEYWORD = new SqlKeywordElementType("SESSION_USER", SqlLanguage.INSTANCE);
	IElementType SET_KEYWORD = new SqlKeywordElementType("SET", SqlLanguage.INSTANCE);
	IElementType SIGNAL_KEYWORD = new SqlKeywordElementType("SIGNAL", SqlLanguage.INSTANCE);
	IElementType SIZE_KEYWORD = new SqlKeywordElementType("SIZE", SqlLanguage.INSTANCE);
	IElementType SMALLINT_KEYWORD = new SqlKeywordElementType("SMALLINT", SqlLanguage.INSTANCE);
	IElementType SOME_KEYWORD = new SqlKeywordElementType("SOME", SqlLanguage.INSTANCE);
	IElementType SPACE_KEYWORD = new SqlKeywordElementType("SPACE", SqlLanguage.INSTANCE);
	IElementType SPECIFIC_KEYWORD = new SqlKeywordElementType("SPECIFIC", SqlLanguage.INSTANCE);
	IElementType SQL_KEYWORD = new SqlKeywordElementType("SQL", SqlLanguage.INSTANCE);
	IElementType SQLCODE_KEYWORD = new SqlKeywordElementType("SQLCODE", SqlLanguage.INSTANCE);
	IElementType SQLERROR_KEYWORD = new SqlKeywordElementType("SQLERROR", SqlLanguage.INSTANCE);
	IElementType SQLEXCEPTION_KEYWORD = new SqlKeywordElementType("SQLEXCEPTION", SqlLanguage.INSTANCE);
	IElementType SQLSTATE_KEYWORD = new SqlKeywordElementType("SQLSTATE", SqlLanguage.INSTANCE);
	IElementType SQLWARNING_KEYWORD = new SqlKeywordElementType("SQLWARNING", SqlLanguage.INSTANCE);
	IElementType SUBSTRING_KEYWORD = new SqlKeywordElementType("SUBSTRING", SqlLanguage.INSTANCE);
	IElementType SUM_KEYWORD = new SqlKeywordElementType("SUM", SqlLanguage.INSTANCE);
	IElementType SYSTEM_USER_KEYWORD = new SqlKeywordElementType("SYSTEM_USER", SqlLanguage.INSTANCE);
	IElementType TABLE_KEYWORD = new SqlKeywordElementType("TABLE", SqlLanguage.INSTANCE);
	IElementType TEMPORARY_KEYWORD = new SqlKeywordElementType("TEMPORARY", SqlLanguage.INSTANCE);
	IElementType THEN_KEYWORD = new SqlKeywordElementType("THEN", SqlLanguage.INSTANCE);
	IElementType TIME_KEYWORD = new SqlKeywordElementType("TIME", SqlLanguage.INSTANCE);
	IElementType TIMESTAMP_KEYWORD = new SqlKeywordElementType("TIMESTAMP", SqlLanguage.INSTANCE);
	IElementType TIMEZONE_HOUR_KEYWORD = new SqlKeywordElementType("TIMEZONE_HOUR", SqlLanguage.INSTANCE);
	IElementType TIMEZONE_MINUTE_KEYWORD = new SqlKeywordElementType("TIMEZONE_MINUTE", SqlLanguage.INSTANCE);
	IElementType TO_KEYWORD = new SqlKeywordElementType("TO", SqlLanguage.INSTANCE);
	IElementType TRAILING_KEYWORD = new SqlKeywordElementType("TRAILING", SqlLanguage.INSTANCE);
	IElementType TRANSACTION_KEYWORD = new SqlKeywordElementType("TRANSACTION", SqlLanguage.INSTANCE);
	IElementType TRANSLATE_KEYWORD = new SqlKeywordElementType("TRANSLATE", SqlLanguage.INSTANCE);
	IElementType TRANSLATION_KEYWORD = new SqlKeywordElementType("TRANSLATION", SqlLanguage.INSTANCE);
	IElementType TRIM_KEYWORD = new SqlKeywordElementType("TRIM", SqlLanguage.INSTANCE);
	IElementType TRUE_KEYWORD = new SqlKeywordElementType("TRUE", SqlLanguage.INSTANCE);
	IElementType UNDO_KEYWORD = new SqlKeywordElementType("UNDO", SqlLanguage.INSTANCE);
	IElementType UNION_KEYWORD = new SqlKeywordElementType("UNION", SqlLanguage.INSTANCE);
	IElementType UNIQUE_KEYWORD = new SqlKeywordElementType("UNIQUE", SqlLanguage.INSTANCE);
	IElementType UNKNOWN_KEYWORD = new SqlKeywordElementType("UNKNOWN", SqlLanguage.INSTANCE);
	IElementType UNTIL_KEYWORD = new SqlKeywordElementType("UNTIL", SqlLanguage.INSTANCE);
	IElementType UPDATE_KEYWORD = new SqlKeywordElementType("UPDATE", SqlLanguage.INSTANCE);
	IElementType UPPER_KEYWORD = new SqlKeywordElementType("UPPER", SqlLanguage.INSTANCE);
	IElementType USAGE_KEYWORD = new SqlKeywordElementType("USAGE", SqlLanguage.INSTANCE);
	IElementType USER_KEYWORD = new SqlKeywordElementType("USER", SqlLanguage.INSTANCE);
	IElementType USING_KEYWORD = new SqlKeywordElementType("USING", SqlLanguage.INSTANCE);
	IElementType VALUE_KEYWORD = new SqlKeywordElementType("VALUE", SqlLanguage.INSTANCE);
	IElementType VALUES_KEYWORD = new SqlKeywordElementType("VALUES", SqlLanguage.INSTANCE);
	IElementType VARCHAR_KEYWORD = new SqlKeywordElementType("VARCHAR", SqlLanguage.INSTANCE);
	IElementType VARYING_KEYWORD = new SqlKeywordElementType("VARYING", SqlLanguage.INSTANCE);
	IElementType VIEW_KEYWORD = new SqlKeywordElementType("VIEW", SqlLanguage.INSTANCE);
	IElementType WHEN_KEYWORD = new SqlKeywordElementType("WHEN", SqlLanguage.INSTANCE);
	IElementType WHENEVER_KEYWORD = new SqlKeywordElementType("WHENEVER", SqlLanguage.INSTANCE);
	IElementType WHERE_KEYWORD = new SqlKeywordElementType("WHERE", SqlLanguage.INSTANCE);
	IElementType WHILE_KEYWORD = new SqlKeywordElementType("WHILE", SqlLanguage.INSTANCE);
	IElementType WITH_KEYWORD = new SqlKeywordElementType("WITH", SqlLanguage.INSTANCE);
	IElementType WORK_KEYWORD = new SqlKeywordElementType("WORK", SqlLanguage.INSTANCE);
	IElementType WRITE_KEYWORD = new SqlKeywordElementType("WRITE", SqlLanguage.INSTANCE);
	IElementType YEAR_KEYWORD = new SqlKeywordElementType("YEAR", SqlLanguage.INSTANCE);
	IElementType ZONE_KEYWORD = new SqlKeywordElementType("ZONE", SqlLanguage.INSTANCE);

	static void init()
	{
		// just empty method for initialize constants
	}
}
