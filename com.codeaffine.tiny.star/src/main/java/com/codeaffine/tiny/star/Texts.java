package com.codeaffine.tiny.star;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String TINY_STAR_START_INFO
        = """
        +------------------------------------------------------------------------
        |  TINY STAR
        |  STand-Alone-Runner for RAP Applications
        |
        |  Launching: %s
        |
        |  (c) 2022-%s CA Code Affine GmbH
        |  All rights reserved.
        |  https://www.codeaffine.com
        +------------------------------------------------------------------------
        """;
    static final String ERROR_INVALID_METHOD_SIGNATURE = "Invalid method signature: %s.%s(...). "
        + "%s listeners methods have either no parameter or a single parameter of type %s.";
    static final String ERROR_NOTIFYING_STARTED_LISTENER = "Error while notifying started listener:";
    static final String ERROR_NOTIFYING_STOPPING_LISTENER = "Error while notifying stopping listener:";
    static final String ERROR_NOTIFYING_STOPPED_LISTENER = "Error while notifying stopped listener:";
    static final String ERROR_LISTENER_NOTIFICATION = "%s notifying %s#%s";
    static final String ERROR_TERMINATING_APPLICATION = "Problems occurred during application server shutdown. For details see log messages.";
    static final String ENFORCING_APPLICATION_TERMINATION = "Enforcing application server shutdown.";
    static final String DEBUG_APPLICATION_NOT_HALTED = "Application process is not halted.";
    static final String DEBUG_APPLICATION_NOT_RUNNING = "Application process is not running.";
    static final String INFO_SHUTDOWN_CONFIRMATION = "Stopping application server instance of {} took {} ms.";
    static final String INFO_STARTUP_CONFIRMATION = "Starting {} application process took {} ms.";
    static final String INFO_CREATION_CONFIRMATION = "Creation of {} appliation process took {} ms.";
    static final String INFO_SERVER_USAGE = "Starting {} with embedded {}.";
    static final String INFO_WORKING_DIRECTORY = "Application server working directory: {}";
    static final String ERROR_GIVEN_WORKING_DIRECTORY_DOES_NOT_EXIST = "Given working directory %s does not exist.";
    static final String ERROR_GIVEN_WORKING_DIRECTORY_FILE_IS_NOT_A_DIRECTORY = "Given working directory file %s is not a directory.";
    static final String ERROR_MORE_THAN_ONE_SERVER_FACTORY = "More than one ServerFactory implementation found on classpath: %s";
    static final String ERROR_NO_SERVER_FACTORY_FOUND = "No server factory found.";
    static final String THREAD_NAME_APPLICATION_SERVER_SHUTDOWN_HOOK = "Application Server Shutdown Hook";
    static final String ERROR_READING_ATTRIBUTE = "unable to read attribute '%s' from environment configuration '%s'";
}
