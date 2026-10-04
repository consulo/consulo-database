import org.jspecify.annotations.NullMarked;

/**
 * @author VISTALL
 * @since 08-Jul-22
 */
@NullMarked
module consulo.database.mongo.impl {
    requires consulo.database.datasource.json.api;
    // the Thrift protocol of the runtime process, which runs the MongoDB driver - the IDE loads no driver class
    requires consulo.database.datasource.mongo.rt.shared;

    requires consulo.application.api;
    requires consulo.component.api;
    requires consulo.configurable.api;
    requires consulo.container.api;
    requires consulo.disposer.api;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.platform.api;
    requires consulo.process.api;
    requires consulo.project.api;
    requires consulo.project.ui.view.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.util.concurrent;
    requires consulo.util.dataholder;
    requires consulo.util.io;
    requires consulo.util.xml.serializer;

    requires org.apache.thrift;
    // the runtime class path takes the slf4j jar of the platform, which libthrift needs
    requires org.slf4j;

    opens consulo.database.mongo.transport to consulo.util.xml.serializer;
}
