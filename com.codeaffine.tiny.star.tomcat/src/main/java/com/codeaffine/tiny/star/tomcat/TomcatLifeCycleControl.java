package com.codeaffine.tiny.star.tomcat;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import static com.codeaffine.tiny.star.tomcat.Texts.*;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class TomcatLifeCycleControl {

    @NonNull
    private final Tomcat tomcat;

    void startTomcat() {
        try {
            tomcat.start();
        } catch (LifecycleException cause) {
            throw new IllegalStateException(ERROR_STARTING_TOMCAT, cause);
        }
    }

    void stopTomcat() {
        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (LifecycleException cause) {
            throw new IllegalStateException(ERROR_STOPPING_TOMCAT, cause);
        }
    }
}
