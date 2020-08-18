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

service JdbcExecutor
{
	void connect(1: string url, 2: map<string, string> properties) throws (1: FailError fr);

	bool testConnection() throws (1: FailError fr);

	list<string> listDatabases() throws (1: FailError fr);

	list<JdbcTable> listTables(1: string databaseName) throws (1: FailError fr);
}

