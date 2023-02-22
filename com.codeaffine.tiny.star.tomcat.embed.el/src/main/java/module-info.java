module org.apache.tomcat.embed.el {
    requires com.codeaffine.tiny.star.tomcat.bnd.dummy;
    requires java.base;
    requires java.desktop;

    requires transitive jakarta.el;

    exports org.apache.el;
    exports org.apache.el.lang;
    exports org.apache.el.parser;

    provides jakarta.el.ExpressionFactory with
        org.apache.el.ExpressionFactoryImpl;
}
