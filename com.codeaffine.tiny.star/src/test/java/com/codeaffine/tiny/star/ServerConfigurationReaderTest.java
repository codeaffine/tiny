package com.codeaffine.tiny.star;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
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

    private Supplier<String> configurationReader;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        configurationReader = mock(Supplier.class);
        ServerConfigurationReader.setConfigurationReader(configurationReader);
    }

    @AfterEach
    void tearDown() {
        ServerConfigurationReader.resetConfigurationReader();
    }

    @Test
    void readEnvironmentConfigurationAttribute() {
        stubConfigurationReaderGet(SERIALIZED);

        String actual = ServerConfigurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeIfAttributeNotSet() {
        stubConfigurationReaderGet(SERIALIZED);

        String actual = ServerConfigurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeIfEnvironmentVariableIsNotSet() {
        stubConfigurationReaderGet(null);

        String actual = ServerConfigurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsDefaultValueArgument() {
        String actual = ServerConfigurationReader.readEnvironmentConfigurationAttribute("unknown", null, String.class);

        assertThat(actual).isNull();
    }

    @Test
    void readEnvironmentConfigurationAttributeWithInvalidFormattedJson() {
        stubConfigurationReaderGet(INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP);

        Exception actual
            = catchException(() -> ServerConfigurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(JsonProcessingException.class)
            .hasMessageContaining(ATTRibUTE_NAME)
            .hasMessageContaining(INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP);
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgument() {
        stubConfigurationReaderGet(SERIALIZED);

        String actual = ServerConfigurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgumentIfAttributeNotSet() {
        stubConfigurationReaderGet(SERIALIZED);

        String actual = ServerConfigurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgumentIfEnvironmentVariableIsNotSet() {
        stubConfigurationReaderGet(null);

        String actual = ServerConfigurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }
    @Test
    void readEnvironmentConfigurationAttributeWithNullAsDefaultValueArgumentUsingFactory() {
        String actual = ServerConfigurationReader.readEnvironmentConfigurationAttribute("unknown", null, String.class);

        assertThat(actual).isNull();
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgumentWithInvalidFormattedJson() {
        stubConfigurationReaderGet(INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP);

        Exception actual
            = catchException(() -> ServerConfigurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String::valueOf));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(JsonProcessingException.class)
            .hasMessageContaining(ATTRibUTE_NAME)
            .hasMessageContaining(INVALID_JSON_ATTRIBUTE_TO_VALUE_MAP);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsAttributeArgument() {
        assertThatThrownBy(() -> ServerConfigurationReader.readEnvironmentConfigurationAttribute(null, ATTRIBUTE_DEFAULT_VALUE, String.class))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsAttributeArgumentUsingFactory() {
        assertThatThrownBy(() -> ServerConfigurationReader.readEnvironmentConfigurationAttribute(null, ATTRIBUTE_DEFAULT_VALUE, String::valueOf))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsFactoryArgument() {
        Function<String, String> factory = null;

        assertThatThrownBy(() -> ServerConfigurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, factory))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsTypeArgument() {
        assertThatThrownBy(() -> ServerConfigurationReader.readEnvironmentConfigurationAttribute(ATTRibUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, (Class<String>) null))
            .isInstanceOf(NullPointerException.class);
    }

    private void stubConfigurationReaderGet(String jsonAttributeToValueMap) {
        when(configurationReader.get()).thenReturn(jsonAttributeToValueMap);
    }
}
