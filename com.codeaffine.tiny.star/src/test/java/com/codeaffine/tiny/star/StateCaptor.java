/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationServer.*;

import lombok.Getter;

@Getter
class StateCaptor {

    private State halted;
    private State starting;
    private State started;
    private State stopping;

    @Stopped
    void captureHalted(ApplicationServer applicationServer) {
        halted = applicationServer.getState();
    }

    @Starting
    void captureStarting(ApplicationServer applicationServer) {
        starting = applicationServer.getState();
    }

    @Started
    void captureStarted(ApplicationServer applicationServer) {
        started = applicationServer.getState();
    }

    @Stopping
    void captureStopping(ApplicationServer applicationServer) {
        stopping = applicationServer.getState();
    }

    void clear() {
        halted = null;
        starting = null;
        started = null;
        stopping = null;
    }
}
