module com.codeaffine.tiny.star.tomcat {
    requires static lombok;

    requires jakarta.servlet;
    requires com.codeaffine.tiny.star;
    requires com.codeaffine.tiny.star.servlet;
    requires org.eclipse.rap.rwt;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.jasper;

    provides com.codeaffine.tiny.star.spi.ServerFactory
        with com.codeaffine.tiny.star.tomcat.ServerFactoryImpl;
}
