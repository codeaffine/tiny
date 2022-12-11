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
