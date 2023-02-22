module com.codeaffine.tiny.star.undertow {
    requires static lombok;

    requires com.codeaffine.tiny.shared;
    requires com.codeaffine.tiny.star;
    requires com.codeaffine.tiny.star.servlet;
    requires jakarta.servlet;
    requires jdk.unsupported;
    requires org.eclipse.rap.rwt;
    requires undertow.core;
    requires undertow.servlet;

    provides com.codeaffine.tiny.star.spi.ServerFactory
        with com.codeaffine.tiny.star.undertow.ServerFactoryImpl;
}
