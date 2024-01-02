/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.util.NoSuchElementException;
import java.util.Scanner;

import static com.codeaffine.tiny.star.cli.Texts.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

@RequiredArgsConstructor(access = PACKAGE)
class InputScanner {

    private static final String STOP_SIGNAL = CommandLineInterface.class.getName() + "#stop\n";

    @NonNull
    private final CommandDispatcher commandDispatcher;
    @NonNull
    private final Logger logger;

    private volatile boolean stopping;
    private CancelableInputStream cancelableInputStream;

    InputScanner(CommandDispatcher commandDispatcher) {
        this(commandDispatcher, getLogger(InputScanner.class));
    }

    void cancel() {
        if (nonNull(cancelableInputStream)) {
            cancelableInputStream.cancel();
        }
    }

    void scanForCommandCode() {
        cancelableInputStream = new CancelableInputStream(System.in, STOP_SIGNAL);
        try (Scanner commandScanner = new Scanner(cancelableInputStream)) {
            logger.debug(DEBUG_START_SCANNING_FOR_COMMANDS);
            while (!stopping) {
                String line = null;
                try {
                    line = commandScanner.next();
                } catch (NoSuchElementException | IllegalStateException e) {
                    stopping = true;
                }
                if (isNull(line) || STOP_SIGNAL.trim().equals(line.trim())) {
                    stopping = true;
                } else {
                    logger.debug(DEBUG_DISPATCHING_COMMAND, line);
                    commandDispatcher.dispatchCommand(line);
                }
            }
            logger.debug(DEBUG_END_SCANNING_FOR_COMMANDS);
        }
    }
}
