package com.codeaffine.tiny.star;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.ApplicationServer.State.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationServerCompatibilityContractTestHelperTest {

    @ParameterizedTest(name = "[{index}] awaiting state {2} with server having initial state {0} and retry state {1} expects server to provide"
        + " state {3} after call.")
    @CsvSource({
        "RUNNING, STOPPING, RUNNING, STOPPING",
        "HALTED, RUNNING, RUNNING, STOPPING",
        "HALTED, STARTING, RUNNING, HALTED",
    })
    void awaitState(State initialState, State stateAfterOneRetry, State toWaitFor, State expectedState) {
        ApplicationServer applicationServer = mock(ApplicationServer.class);
        when(applicationServer.getState()).thenReturn(initialState, stateAfterOneRetry, STOPPING, HALTED);

        ApplicationServerCompatibilityContractUtil.awaitState(applicationServer, toWaitFor, 1, 40L);
        State actual = applicationServer.getState();

        assertThat(actual).isSameAs(expectedState);
    }
}
