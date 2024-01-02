/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType.CLASSPATH;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.mock;

class MultiServerConfigurationReaderTest implements ServerConfigurationReaderContractTest {

    private static final String APPLICATION_SERVER_ID = "applicationServerId";
    private static final String MULTI_SERVER_CONFIGURATION_FORMAT = "{\"%s\":%s}";

    @Override
    public ServerConfigurationReader newServerConfigurationReader(String serialized) {
        return new MultiServerConfigurationReader(APPLICATION_SERVER_ID, () -> serialized, mock(ApplicationConfiguration.class));
    }

    @Override
    public String getConfigurationJson(KeyStoreLocationType keyStoreLocationType, String keyStoreFile) {
        return format(
            MULTI_SERVER_CONFIGURATION_FORMAT,
            APPLICATION_SERVER_ID,
            createConfigurationJson(
                keyStoreLocationType,
                keyStoreFile)
        );
    }

    @Test
    void readEnvironmentConfigurationAttributeIfApplicationServerIdCannotBeFound() {
        String configurationWithoutServerId = format(
            MULTI_SERVER_CONFIGURATION_FORMAT,
            "unknownApplicationServerId",
            createConfigurationJson(
                CLASSPATH,
                KEY_STORE_FILE_ON_CLASSPATH)
        );
        ServerConfigurationReader configurationReader = newServerConfigurationReader(configurationWithoutServerId);

        String actual = configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    void readEnvironmentConfigurationAttributeIfSerializationFormatIsNotValid() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(INVALID_SERIALISATION_FORMAT);

        Exception actual
            = catchException(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(JsonProcessingException.class)
            .hasMessageContaining(APPLICATION_SERVER_ID)
            .hasMessageContaining(INVALID_SERIALISATION_FORMAT);
    }
}
