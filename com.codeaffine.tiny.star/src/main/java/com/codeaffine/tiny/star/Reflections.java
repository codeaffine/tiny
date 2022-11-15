package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
class Reflections {

    static <T extends RuntimeException> RuntimeException extractExceptionToReport(Exception exception, Function<Throwable, T> runtimeExceptionFactory) {
        RuntimeException result;
        if(exception instanceof RuntimeException runtimeException) {
            result = runtimeException;
        } else if (exception instanceof InvocationTargetException invocationTargetException) {
            if (invocationTargetException.getCause() instanceof RuntimeException runtimeException) {
                result = runtimeException;
            } else {
                result = runtimeExceptionFactory.apply(invocationTargetException.getCause());
            }
        } else {
            result = runtimeExceptionFactory.apply(exception);
        }
        return result;
    }
}
