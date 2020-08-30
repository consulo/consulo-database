namespace * consulo.database.jdbc.rt.shared

exception FailError
{
	1: string message;

	2: string trace;
}

struct JdbcTable
{
	1: string name;

	2: list<JdbcColum> colums;
}

struct JdbcColum
{
	1: string name;

	2: string type;
}

struct JdbcQueryResult
{
	1: list<string> columns;

	2: list<JdbcQueryRow> rows;
}

struct JdbcQueryRow
{
	1: list<JdbcValue> values;
}

enum JdbcValueType
{
	_int,
	_string,
	_bool,
	_long
}

struct JdbcValue
{
	1: JdbcValueType type;

	2: optional i32 intValue;

	3: optional string stringValue;

	4: optional bool boolValue;

	5: optional i64 longValue;
}

service JdbcExecutor
{
	void connect(1: string url, 2: map<string, string> properties) throws (1: FailError fr);

	bool testConnection() throws (1: FailError fr);

	list<string> listDatabases() throws (1: FailError fr);

	list<JdbcTable> listTables(1: string databaseName) throws (1: FailError fr);

	JdbcQueryResult runQuery(1: string query, 2: list<JdbcValue> params) throws (1: FailError fr);
}

