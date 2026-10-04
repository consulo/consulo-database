/**
 * The Thrift protocol between the IDE and the MongoDB runtime process. The code in consulo.database.mongo.rt.shared is generated
 * from MongoExecutor.thrift.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
module consulo.database.datasource.mongo.rt.shared {
    requires org.apache.thrift;

    // the generated processor logs through slf4j
    requires org.slf4j;

    exports consulo.database.mongo.rt.shared;
}
