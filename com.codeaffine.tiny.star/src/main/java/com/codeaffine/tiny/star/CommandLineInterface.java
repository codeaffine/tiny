package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationInstance.Started;
import static org.slf4j.LoggerFactory.getLogger;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import org.slf4j.Logger;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class CommandLineInterface {

    private final Logger logger;

    public CommandLineInterface() {
        logger = getLogger(CommandLineInterface.class);
    }

    @Started
    public void startCli(ApplicationInstance applicationInstance) {
        ExecutorService executor = newSingleThreadExecutor();
        executor.execute(() -> {
            logger.atInfo().log("Press q to stop instance of {}.", applicationInstance.getName());
            try (Scanner scanner = new Scanner(System.in)) {
                String line = null;
                while (!"q".equals(line)) {
                    line = scanner.next();
                }
            }
            applicationInstance.stop();
            executor.shutdown();
        });
    }
}
