/*
 * Copyright 2013-2026 consulo.io
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

package consulo.sql.lang.impl.postgresql;

import consulo.sql.lang.impl.psi.SqlKeywordElementType;
import consulo.sql.lang.impl.psi.SqlKeywordTokenTypes;

/**
 * PostgreSQL-specific reserved keywords (beyond SQL-92).
 *
 * @author VISTALL
 * @since 2026-03-17
 */
public interface PostgreSqlTokenTypes {
    SqlKeywordElementType[] RESERVED_KEYWORDS = {
        SqlKeywordTokenTypes.ABORT_KEYWORD,
        SqlKeywordTokenTypes.ANALYSE_KEYWORD,
        SqlKeywordTokenTypes.ANALYZE_KEYWORD,
        SqlKeywordTokenTypes.ARRAY_KEYWORD,
        SqlKeywordTokenTypes.BIGINT_KEYWORD,
        SqlKeywordTokenTypes.BOOLEAN_KEYWORD,
        SqlKeywordTokenTypes.BYTEA_KEYWORD,
        SqlKeywordTokenTypes.CACHE_KEYWORD,
        SqlKeywordTokenTypes.CHECKPOINT_KEYWORD,
        SqlKeywordTokenTypes.CLUSTER_KEYWORD,
        SqlKeywordTokenTypes.COMMENT_KEYWORD,
        SqlKeywordTokenTypes.CONCURRENTLY_KEYWORD,
        SqlKeywordTokenTypes.COPY_KEYWORD,
        SqlKeywordTokenTypes.DATABASE_KEYWORD,
        SqlKeywordTokenTypes.DISABLE_KEYWORD,
        SqlKeywordTokenTypes.ENABLE_KEYWORD,
        SqlKeywordTokenTypes.ENCODING_KEYWORD,
        SqlKeywordTokenTypes.ENUM_KEYWORD,
        SqlKeywordTokenTypes.EXCLUDE_KEYWORD,
        SqlKeywordTokenTypes.EXPLAIN_KEYWORD,
        SqlKeywordTokenTypes.EXTENSION_KEYWORD,
        SqlKeywordTokenTypes.FREEZE_KEYWORD,
        SqlKeywordTokenTypes.GREATEST_KEYWORD,
        SqlKeywordTokenTypes.ILIKE_KEYWORD,
        SqlKeywordTokenTypes.IMMUTABLE_KEYWORD,
        SqlKeywordTokenTypes.INCREMENT_KEYWORD,
        SqlKeywordTokenTypes.INDEX_KEYWORD,
        SqlKeywordTokenTypes.INHERIT_KEYWORD,
        SqlKeywordTokenTypes.INHERITS_KEYWORD,
        SqlKeywordTokenTypes.ISNULL_KEYWORD,
        SqlKeywordTokenTypes.LATERAL_KEYWORD,
        SqlKeywordTokenTypes.LEAST_KEYWORD,
        SqlKeywordTokenTypes.LIMIT_KEYWORD,
        SqlKeywordTokenTypes.LISTEN_KEYWORD,
        SqlKeywordTokenTypes.LOCK_KEYWORD,
        SqlKeywordTokenTypes.MATERIALIZED_KEYWORD,
        SqlKeywordTokenTypes.MOVE_KEYWORD,
        SqlKeywordTokenTypes.NOTHING_KEYWORD,
        SqlKeywordTokenTypes.NOTIFY_KEYWORD,
        SqlKeywordTokenTypes.NOTNULL_KEYWORD,
        SqlKeywordTokenTypes.NOWAIT_KEYWORD,
        SqlKeywordTokenTypes.OFFSET_KEYWORD,
        SqlKeywordTokenTypes.OPERATOR_KEYWORD,
        SqlKeywordTokenTypes.OWNED_KEYWORD,
        SqlKeywordTokenTypes.OWNER_KEYWORD,
        SqlKeywordTokenTypes.PARALLEL_KEYWORD,
        SqlKeywordTokenTypes.PARTITION_KEYWORD,
        SqlKeywordTokenTypes.PLACING_KEYWORD,
        SqlKeywordTokenTypes.POLICY_KEYWORD,
        SqlKeywordTokenTypes.REASSIGN_KEYWORD,
        SqlKeywordTokenTypes.REINDEX_KEYWORD,
        SqlKeywordTokenTypes.RENAME_KEYWORD,
        SqlKeywordTokenTypes.REPLACE_KEYWORD,
        SqlKeywordTokenTypes.REPLICA_KEYWORD,
        SqlKeywordTokenTypes.RESET_KEYWORD,
        SqlKeywordTokenTypes.RETURNING_KEYWORD,
        SqlKeywordTokenTypes.RULE_KEYWORD,
        SqlKeywordTokenTypes.SEQUENCE_KEYWORD,
        SqlKeywordTokenTypes.SERIAL_KEYWORD,
        SqlKeywordTokenTypes.SHARE_KEYWORD,
        SqlKeywordTokenTypes.SHOW_KEYWORD,
        SqlKeywordTokenTypes.SNAPSHOT_KEYWORD,
        SqlKeywordTokenTypes.STABLE_KEYWORD,
        SqlKeywordTokenTypes.STATISTICS_KEYWORD,
        SqlKeywordTokenTypes.STORAGE_KEYWORD,
        SqlKeywordTokenTypes.STRICT_KEYWORD,
        SqlKeywordTokenTypes.TABLESPACE_KEYWORD,
        SqlKeywordTokenTypes.TEMP_KEYWORD,
        SqlKeywordTokenTypes.TEXT_KEYWORD,
        SqlKeywordTokenTypes.TRIGGER_KEYWORD,
        SqlKeywordTokenTypes.TRUNCATE_KEYWORD,
        SqlKeywordTokenTypes.UNLOGGED_KEYWORD,
        SqlKeywordTokenTypes.VACUUM_KEYWORD,
        SqlKeywordTokenTypes.VALIDATE_KEYWORD,
        SqlKeywordTokenTypes.VARIADIC_KEYWORD,
        SqlKeywordTokenTypes.VERBOSE_KEYWORD,
        SqlKeywordTokenTypes.VOLATILE_KEYWORD,
        SqlKeywordTokenTypes.WINDOW_KEYWORD,
    };
}
