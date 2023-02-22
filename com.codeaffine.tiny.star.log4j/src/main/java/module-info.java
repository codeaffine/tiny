module com.codeaffine.tiny.star.log4j {
    requires static lombok;

    requires com.codeaffine.tiny.shared;
    requires com.codeaffine.tiny.star;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.slf4j;

    provides com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory
        with com.codeaffine.tiny.star.log4j.Log4j2LoggingFrameworkControlFactory;
}
