module com.codeaffine.tiny.star.cli {
    requires static lombok;

    requires com.codeaffine.tiny.shared;
    requires com.codeaffine.tiny.star;
    requires org.slf4j;

    uses com.codeaffine.tiny.star.cli.spi.CliCommandProvider;
}
