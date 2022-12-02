package com.codeaffine.tiny.star.spi;

import com.codeaffine.tiny.star.ApplicationInstance;

public interface CliCommand {
    default String getId() {
        return getClass().getName();
    }
    String getCode();
    String getName();
    String getDescription(String code, ApplicationInstance applicationInstance);
    default boolean printHelpOnStartup() {
        return false;
    }
    default boolean isHelpCommand() {
        return false;
    }
    void execute(ApplicationInstance applicationInstance);
}
