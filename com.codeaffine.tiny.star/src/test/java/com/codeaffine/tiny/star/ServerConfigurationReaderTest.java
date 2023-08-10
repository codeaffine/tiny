/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServerConfigurationReaderTest {

    private static final String ATTRibUTE_NAME = "attribute";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_DEFAULT_VALUE = "defaultValue";
    private static final String SERIALIZED = format("{\"%s\":\"%s\"}", ATTRibUTE_NAME, ATTRIBUTE_VALUE);
    private static final String INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP = "invalid";

    private ServerConfigurationReader configurationReader;
    private Supplier<String> configurationLoader;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        configurationLoader = mock(Supplier.class);
        configurationReader = new ServerConfigurationReader(configurationLoader);
    }

    @Test
    void readEnvironmentConfigurationAttribute() {
        stubConfigurationReaderGet(SERIALIZED);

        String actual = configurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeIfAttributeNotSet() {
        stubConfigurationReaderGet(SERIALIZED);

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeIfEnvironmentVariableIsNotSet() {
        stubConfigurationReaderGet(null);

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsDefaultValueArgument() {
        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", null, String.class);

        assertThat(actual).isNull();
    }

    @Test
    void readEnvironmentConfigurationAttributeWithInvalidFormattedJson() {
        stubConfigurationReaderGet(INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP);

        Exception actual
            = catchException(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(JsonProcessingException.class)
            .hasMessageContaining(ATTRibUTE_NAME)
            .hasMessageContaining(INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP);
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgument() {
        stubConfigurationReaderGet(SERIALIZED);

        String actual = configurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgumentIfAttributeNotSet() {
        stubConfigurationReaderGet(SERIALIZED);

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgumentIfEnvironmentVariableIsNotSet() {
        stubConfigurationReaderGet(null);

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }
    @Test
    void readEnvironmentConfigurationAttributeWithNullAsDefaultValueArgumentUsingFactory() {
        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", null, String.class);

        assertThat(actual).isNull();
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgumentWithInvalidFormattedJson() {
        stubConfigurationReaderGet(INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP);

        Exception actual
            = catchException(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String::valueOf));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(JsonProcessingException.class)
            .hasMessageContaining(ATTRibUTE_NAME)
            .hasMessageContaining(INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsAttributeArgument() {
        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(null, ATTRIBUTE_DEFAULT_VALUE, String.class))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsAttributeArgumentUsingFactory() {
        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(null, ATTRIBUTE_DEFAULT_VALUE, String::valueOf))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsFactoryArgument() {
        Function<String, String> factory = null;

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, factory))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsTypeArgument() {
        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, (Class<String>) null))
            .isInstanceOf(NullPointerException.class);
    }

    private void stubConfigurationReaderGet(String jsonAttributeToValueMap) {
        when(configurationLoader.get()).thenReturn(jsonAttributeToValueMap);
    }
}
