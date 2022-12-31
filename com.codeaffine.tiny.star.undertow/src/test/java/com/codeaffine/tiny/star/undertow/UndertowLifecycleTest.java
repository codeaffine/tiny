package com.codeaffine.tiny.star.undertow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.star.common.IoUtils.findFreePort;
import static io.undertow.Handlers.path;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UndertowLifecycleTest {

    private UndertowLifecycle lifecycle;

    @AfterEach
    void tearDown() {
        if(nonNull(lifecycle)) {
            lifecycle.stopUndertow();
        }
    }

    @Test
    void construct() {
        lifecycle = assertDoesNotThrow(() -> new UndertowLifecycle("localhost", 1234));
    }

    @Test
    void constructWithNullAsHostArgument() {
        assertThatThrownBy(() -> new UndertowLifecycle(null, 1234))
            .isInstanceOf(NullPointerException.class);
    }

    @Nested
    class Started {

        @BeforeEach
        void setUp() {
            lifecycle = new UndertowLifecycle("localhost", findFreePort());
            lifecycle.startUndertow(path());
        }

        @Test
        void serverExist() {
            assertThat(lifecycle.getServer()).isNotNull();
        }
    }

    @Nested
    class Stopped {

        @BeforeEach
        void setUp() {
            lifecycle = new UndertowLifecycle("localhost", findFreePort());
            lifecycle.startUndertow(path());
            lifecycle.stopUndertow();
        }

        @Test
        void serverDoesNotExist() {
            assertThat(lifecycle.getServer()).isNull();
        }
    }
}
