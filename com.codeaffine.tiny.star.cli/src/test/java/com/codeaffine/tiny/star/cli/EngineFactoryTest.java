/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class EngineFactoryTest {

    @Test
    void createEngine() {
        ExecutorServiceAdapter adapter = mock(ExecutorServiceAdapter.class);
        EngineFactory factory = new EngineFactory(() -> adapter);

        Engine actual = factory.createEngine();

        assertThat(actual).isNotNull();
    }

    @Test
    void constructWithNullAsExecutorFactoryArgument() {
        assertThatThrownBy(() -> new EngineFactory(null))
            .isInstanceOf(NullPointerException.class);
    }
}
