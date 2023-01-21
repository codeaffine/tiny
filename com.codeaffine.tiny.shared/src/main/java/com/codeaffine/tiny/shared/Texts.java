package com.codeaffine.tiny.shared;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_NULL = "Argument runtimeExceptionFactory must not return null on invocation.";
    static final String ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_WRAPPER_WITHOUT_CAUSE
        = "Argument runtimeExceptionFactory must not return an exception without cause.";
    static final String ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_WRAPPER_WITH_WRONG_CAUSE
        = "Argument runtimeExceptionFactory must not return an exception with another cause than the given one.";
    static final String ERROR_TIMEOUT_CALLING_RUNNABLE = "Timeout of %d %s exceeded calling runnable instance of %s";
    static final String ERROR_DIRECTORY_NAME_PREFIX_IS_EMPTY = "Argument 'directoryNamePrefix' must not be empty.";
    static final String ERROR_UNABLE_TO_CREATE_TEMPORARY_DIRECTORY = "Unable to create temporay directory with filename prefix %s.";
    static final String ERROR_UNABLE_TO_DELETE_FILE = "Could not delete file or directory '%s'.";
}
