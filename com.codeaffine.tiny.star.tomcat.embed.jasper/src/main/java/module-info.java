module org.apache.tomcat.embed.jasper {
    requires ant;
    requires org.eclipse.jdt.core.compiler.batch;
    requires java.base;
    requires java.desktop;
    requires java.xml;
    requires jakarta.el;
    requires jakarta.servlet;
    requires jakarta.servlet.jsp;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.el;
}
