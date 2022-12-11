package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import static java.util.Objects.nonNull;

import org.slf4j.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class StartInfoPrinter {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    private final Logger logger;

    StartInfoPrinter(ApplicationServer applicationServer) {
        this(applicationServer, getLogger(StartInfoPrinter.class));
    }

    void printStartText() {
        if (nonNull(applicationServer.startInfoProvider)) {
            applicationServer.startInfoProvider
                .apply(applicationServer)
                .lines()
                .forEach(logger::info);
        }
    }
}
