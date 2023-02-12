module com.codeaffine.tiny.star {
    requires static lombok;

    requires com.codeaffine.tiny.shared;
    requires com.fasterxml.jackson.databind;
    requires org.eclipse.rap.rwt;
    requires org.slf4j;

    exports com.codeaffine.tiny.star;
    exports com.codeaffine.tiny.star.spi;

    uses com.codeaffine.tiny.star.spi.ServerFactory;
    uses com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
}
