namespace * consulo.database.jdbc.rt.shared

exception FailError
{
	1: string message;

	2: string trace;
}

service JdbcExecutor
{
	bool testConnection(1: string url, 2: map<string, string> properties) throws (1: FailError fr);
}

