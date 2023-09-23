/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SingleServerConfigurationReaderTest implements ServerConfigurationReaderContractTest {

    @Override
    public ServerConfigurationReader newServerConfigurationReader(String serialized) {
        return new SingleServerConfigurationReader(() -> serialized, mock(ApplicationConfiguration.class));
    }

    @Override
    public String getConfigurationJson(KeyStoreLocationType keyStoreLocationType, String keyStoreFile) {
        return createConfigurationJson(keyStoreLocationType, keyStoreFile);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithInvalidSerializationFormat() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(INVALID_SERIALISATION_FORMAT);

        Exception actual
            = catchException(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(JsonProcessingException.class)
            .hasMessageContaining(ATTRIBUTE_NAME)
            .hasMessageContaining(INVALID_SERIALISATION_FORMAT);
    }

    @Test
    void readEnvironmentConfigurationAttributeWithNullAsDefaultValueArgumentUsingFactoryAndEnvironmentVariableNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(null);

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", null, String.class);

        assertThat(actual).isNull();
    }

    @Test
    void readEnvironmentConfigurationAttributeUsingFactoryArgumentWithInvalidSerializationFormat() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(INVALID_SERIALISATION_FORMAT);

        Exception actual
            = catchException(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String::valueOf));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(JsonProcessingException.class)
            .hasMessageContaining(ATTRIBUTE_NAME)
            .hasMessageContaining(INVALID_SERIALISATION_FORMAT);
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new SingleServerConfigurationReader((Map<String, ?>) null, mock(ApplicationConfiguration.class)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsApplicationConfigurationArgument() {
        Map<String, Object> attributeMap = emptyMap();

        assertThatThrownBy(() -> new SingleServerConfigurationReader(attributeMap, null))
            .isInstanceOf(NullPointerException.class);
    }
}
