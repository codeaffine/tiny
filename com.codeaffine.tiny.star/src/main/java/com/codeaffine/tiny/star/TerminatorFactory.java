package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class TerminatorFactory {

    @NonNull
    private final ApplicationServer applicationServer;

    Terminator create(File workingDirectory, Server server, LoggingFrameworkControl loggingFrameworkControl, Runnable shutdownHookRemover) {
        return new Terminator(workingDirectory, server, loggingFrameworkControl, shutdownHookRemover, applicationServer.deleteWorkingDirectoryOnShutdown);
    }
}
