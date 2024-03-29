module org.apache.tomcat.embed.core {

    requires com.codeaffine.tiny.star.tomcat.bnd.dummy;
    requires jakartaee.migration;
    requires jakarta.annotation;
    requires jakarta.ejb;
    requires jakarta.mail;
    requires jakarta.persistence;
    requires jakarta.xml.rpc.api;
    requires jakarta.xml.ws;
    requires java.base;
    requires java.desktop;
    requires java.instrument;
    requires java.logging;
    requires java.management;
    requires java.naming;
    requires java.rmi;
    requires java.security.jgss;
    requires java.sql;
    requires java.xml;
    requires wsdl4j;

    requires transitive jakarta.servlet;

    opens org.apache.catalina.core;

    exports org.apache.catalina;
    exports org.apache.catalina.connector;
    exports org.apache.catalina.servlets;
    exports org.apache.catalina.startup;
    exports org.apache.coyote;
    exports org.apache.coyote.http11;
    exports org.apache.juli.logging;
    exports org.apache.tomcat;
    exports org.apache.tomcat.util.buf;
    exports org.apache.tomcat.util.descriptor;
    exports org.apache.tomcat.util.descriptor.tagplugin;
    exports org.apache.tomcat.util.descriptor.web;
    exports org.apache.tomcat.util.digester;
    exports org.apache.tomcat.util.net;
    exports org.apache.tomcat.util.res;
    exports org.apache.tomcat.util.scan;
    exports org.apache.tomcat.util.security;

    uses org.apache.juli.logging.Log;
}
