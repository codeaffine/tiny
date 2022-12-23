package com.codeaffine.tiny.star.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

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
