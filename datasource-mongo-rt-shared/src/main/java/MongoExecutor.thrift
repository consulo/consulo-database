namespace * consulo.database.mongo.rt.shared

/**
 * The runtime prints this prefix and its port on one stdout line once it accepts connections, for example
 * "[mongo-rt-ready] 41234". Nothing else is printed to stdout.
 *
 * Before that the IDE writes two lines to the stdin of the runtime: the client token, then the server token. Both are random
 * and never appear on the command line. The IDE keeps stdin open: when it is closed, the runtime exits.
 */
const string READY_LINE_PREFIX = "[mongo-rt-ready] "

/**
 * The runtime listens on this address only, and the IDE connects to it - not to a host name, which may resolve to another
 * loopback address in the other JVM.
 */
const string LOOPBACK_HOST = "127.0.0.1"

const string MAIN_CLASS = "consulo.database.mongo.rt.Main"

/**
 * Returned by hello(). The IDE refuses a runtime of another version, for example a stale runtime jar.
 */
const i32 PROTOCOL_VERSION = 1

enum MongoErrorKind
{
	INTERNAL = 0,

	NOT_AUTHENTICATED = 1,

	NOT_CONNECTED = 2,

	INVALID_SETTINGS = 3,

	INVALID_QUERY = 4,

	AUTHENTICATION = 5,

	UNAUTHORIZED = 6,

	TIMEOUT = 7,

	CONNECTION = 8,

	SERVER = 9
}

exception MongoFailError
{
	1: string message;

	2: string trace;

	3: MongoErrorKind kind;

	/**
	 * The server error code, for example 13 (Unauthorized) or 50 (MaxTimeMSExpired).
	 */
	4: optional i32 serverCode;
}

/**
 * The answer of hello(): it proves to the IDE that it talks to the runtime it started.
 */
struct MongoHelloResult
{
	/**
	 * PROTOCOL_VERSION of the runtime
	 */
	1: i32 protocolVersion;

	/**
	 * The second stdin line of the runtime. The IDE compares it before it sends anything else, the settings in particular.
	 */
	2: string serverToken;
}

/**
 * The connection settings of a data source. The generated toString() prints every field, the password included: never log it.
 */
struct MongoConnectSettings
{
	/**
	 * A mongodb:// or mongodb+srv:// string without password. When set, host and port are ignored.
	 */
	1: optional string connectionString;

	2: string host;

	3: i32 port;

	4: optional string login;

	5: optional string password;

	6: string authSource;

	/**
	 * connect() applies it only when the connection string sets no serverSelectionTimeoutMS; testConnection() always applies it.
	 */
	7: i32 serverSelectionTimeoutMs;

	/**
	 * Applied only when the connection string sets no connectTimeoutMS.
	 */
	8: i32 connectTimeoutMs;
}

enum MongoNodeKind
{
	/**
	 * Never inside a document - a missing field is simply absent. Reserved for positional cell lists.
	 */
	MISSING = 0,

	NULL = 1,

	UNDEFINED = 2,

	BOOL = 3,

	INT32 = 4,

	INT64 = 5,

	DOUBLE = 6,

	DECIMAL128 = 7,

	STRING = 8,

	DATE = 9,

	TIMESTAMP = 10,

	OBJECT_ID = 11,

	UUID = 12,

	BINARY = 13,

	DOCUMENT = 14,

	ARRAY = 15,

	OTHER = 16
}

/**
 * A BSON value as a tree node.
 */
struct MongoNode
{
	1: MongoNodeKind kind;

	/**
	 * The field name when the parent is a DOCUMENT. Unset for array items and for a root.
	 */
	2: optional string name;

	/**
	 * BOOL
	 */
	3: optional bool boolValue;

	/**
	 * INT32
	 */
	4: optional i32 intValue;

	/**
	 * INT64; DATE: milliseconds since the epoch; TIMESTAMP: seconds << 32 | increment
	 */
	5: optional i64 longValue;

	/**
	 * DOUBLE
	 */
	6: optional double doubleValue;

	/**
	 * STRING; DECIMAL128: Decimal128.toString(), also NaN, Infinity, -Infinity, -0; OBJECT_ID: 24 hex digits;
	 * UUID: the canonical text; OTHER: see typeName
	 */
	7: optional string text;

	/**
	 * BINARY
	 */
	8: optional binary bytes;

	/**
	 * BINARY
	 */
	9: optional i8 binarySubtype;

	/**
	 * OTHER: the $type alias, and what text holds for it:
	 * regex - the pattern, its flags are in options (shown as /pattern/options);
	 * javascript - the code;
	 * javascriptWithScope - the code, the scope is in children;
	 * symbol - the symbol;
	 * dbPointer - the display text DBPointer("namespace", hex);
	 * minKey, maxKey - MinKey, MaxKey;
	 * a type a newer driver adds - its lower case BSON type name, text is the display text
	 */
	10: optional string typeName;

	/**
	 * DOCUMENT: the fields in document order, each with its name. ARRAY: the items. OTHER javascriptWithScope: the fields of
	 * the scope document, each with its name
	 */
	11: optional list<MongoNode> children;

	/**
	 * OTHER regex: the flags, for example "im"; empty when there are none
	 */
	12: optional string options;
}

struct MongoCollectionInfo
{
	1: string name;

	/**
	 * collection, view, timeseries - or another type a newer server reports
	 */
	2: string type;
}

struct MongoFindResult
{
	/**
	 * DOCUMENT nodes
	 */
	1: list<MongoNode> documents;

	/**
	 * The documents stopped before the limit, as their BSON size reached the response budget of the runtime. The first
	 * document is always returned, whatever its size.
	 */
	2: bool truncated;
}

struct MongoFieldShape
{
	1: string name;

	2: MongoNodeKind kind;

	/**
	 * OTHER: the $type alias
	 */
	3: optional string typeName;
}

/**
 * The top level fields of a document, without their values.
 */
struct MongoDocumentShape
{
	/**
	 * The value of _id, unset when the document has none.
	 */
	1: optional MongoNode id;

	2: list<MongoFieldShape> fields;
}

service MongoExecutor
{
	/**
	 * Must be the first call on every connection, every other call fails with NOT_AUTHENTICATED before it. A wrong token fails
	 * with NOT_AUTHENTICATED and every later call on that connection fails too.
	 *
	 * @param clientToken the first stdin line of the runtime
	 */
	MongoHelloResult hello(1: string clientToken) throws (1: MongoFailError fe);

	/**
	 * Creates the client of the process, replacing the previous one. It does not wait for the server.
	 */
	void connect(1: MongoConnectSettings settings) throws (1: MongoFailError fe);

	/**
	 * Pings the auth source with a throw-away client. The client of the process is not touched.
	 */
	void testConnection(1: MongoConnectSettings settings) throws (1: MongoFailError fe);

	/**
	 * Runs {ping: 1} against the auth source with the client of the process.
	 */
	void ping() throws (1: MongoFailError fe);

	/**
	 * The databases the user may read (nameOnly, authorizedDatabases).
	 */
	list<string> listDatabases(1: i32 maxTimeMs) throws (1: MongoFailError fe);

	/**
	 * When listCollections is not authorized, the names of the authorized collections typed "collection"; when that is not
	 * authorized either, an empty list.
	 */
	list<MongoCollectionInfo> listCollections(1: string database, 2: i32 maxTimeMs) throws (1: MongoFailError fe);

	/**
	 * filterJson and sortJson are relaxed JSON (shell syntax). Blank filterJson means {}. The sort always ends with _id - it is
	 * appended unless sortJson has it - so skip/limit paging has a total order.
	 */
	MongoFindResult find(1: string database,
	                     2: string collection,
	                     3: string filterJson,
	                     4: string sortJson,
	                     5: i32 skip,
	                     6: i32 limit,
	                     7: i32 maxTimeMs) throws (1: MongoFailError fe);

	/**
	 * The shapes of $sample random documents. The runtime stops early once the shapes hold too many fields in total, so fewer
	 * shapes than size may come back.
	 */
	list<MongoDocumentShape> sample(1: string database, 2: string collection, 3: i32 size, 4: i32 maxTimeMs) throws (1: MongoFailError fe);

	/**
	 * The count of the collection metadata, which may be stale.
	 */
	i64 estimatedCount(1: string database, 2: string collection, 3: i32 maxTimeMs) throws (1: MongoFailError fe);

	i64 countDocuments(1: string database, 2: string collection, 3: string filterJson, 4: i32 maxTimeMs) throws (1: MongoFailError fe);

	/**
	 * Closes the client and exits the process. Ignored before hello().
	 */
	oneway void shutdown();
}
