package com.codeaffine.tiny.star.spi;

import com.codeaffine.tiny.star.ApplicationServer;

import java.util.Map;


public interface CliCommand {
    String getCode();
    String getName();
    String getDescription(CliCommand command, ApplicationServer applicationServer);
    default boolean printHelpOnStartup() {
        return false;
    }
    default boolean isHelpCommand() {
        return false;
    }
    default void execute(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap){
        execute(applicationServer);
    }
    default void execute(ApplicationServer applicationServer) {
        execute();
    }
    default void execute() {
    }
}
