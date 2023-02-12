module com.codeaffine.tiny.star.cli.basic {
    requires static lombok;

    requires com.codeaffine.tiny.star;
    requires com.codeaffine.tiny.star.cli;

    provides com.codeaffine.tiny.star.cli.spi.CliCommandProvider
        with com.codeaffine.tiny.star.cli.basic.BasicCliCommandProvider;
}
