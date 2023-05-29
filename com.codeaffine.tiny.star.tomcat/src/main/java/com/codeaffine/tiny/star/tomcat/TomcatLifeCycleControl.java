/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import static com.codeaffine.tiny.star.tomcat.Texts.ERROR_STARTING_TOMCAT;
import static com.codeaffine.tiny.star.tomcat.Texts.ERROR_STOPPING_TOMCAT;
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
