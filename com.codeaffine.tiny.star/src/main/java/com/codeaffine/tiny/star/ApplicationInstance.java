package com.codeaffine.tiny.star;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public interface ApplicationInstance {

    enum State { STARTING, RUNNING, STOPPING, HALTED }

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Starting {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Started {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Stopping {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Stopped {}

    String getIdentifier();
    void stop();
    State getState();
}
