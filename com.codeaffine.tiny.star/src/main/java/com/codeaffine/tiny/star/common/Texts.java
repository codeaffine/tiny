package com.codeaffine.tiny.star.common;

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
}
