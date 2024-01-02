/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TomcatLifeCycleControlTest {

    private TomcatLifeCycleControl control;
    private Tomcat tomcat;

    @BeforeEach
    void setUp() {
        tomcat = mock(Tomcat.class);
        control = new TomcatLifeCycleControl(tomcat);
    }

    @Test
    void startTomcat() throws LifecycleException {
        control.startTomcat();

        verify(tomcat).start();
    }

    @Test
    void stopTomcat() throws LifecycleException {
        control.stopTomcat();

        InOrder order = inOrder(tomcat);
        order.verify(tomcat).stop();
        order.verify(tomcat).destroy();
        order.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = { "start", "stop", "destroy" })
    void problemOnTomcatMethodDelegation(String delegateMethodName) throws LifecycleException {
        LifecycleException expected = stubDelegateMethodWithProblem(delegateMethodName, new LifecycleException());

        Exception actual = catchException(() -> delegateToMethodUnderTest(delegateMethodName));

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasCause(expected);
    }

    @Test
    void constructWithNullAsTomcatArgument() {
        assertThatThrownBy(() -> new TomcatLifeCycleControl(null))
            .isInstanceOf(NullPointerException.class);
    }

    private LifecycleException stubDelegateMethodWithProblem(String delegateMethodName, LifecycleException expected) throws LifecycleException {
        Tomcat when = doThrow(expected).when(tomcat);
        switch (delegateMethodName) {
            case "start" -> when.start();
            case "stop" -> when.stop();
            case "destroy" -> when.destroy();
            default -> throw new IllegalArgumentException("Unknown tomcat delegate method name: " + delegateMethodName);
        }
        return expected;
    }

    private void delegateToMethodUnderTest(String delegateMethodName) {
        switch (delegateMethodName) {
            case "start" -> control.startTomcat();
            case "stop", "destroy" -> control.stopTomcat();
            default -> throw new IllegalArgumentException("Unknown tomcat delegate method name: " + delegateMethodName);
        }
    }
}
