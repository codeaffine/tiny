/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

class MultiServerConfigurationReaderTest implements ServerConfigurationReaderContract {

    private static final String APPLICATION_SERVER_ID = "applicationServerId";
    private static final String SERIALIZATION_FORMAT = "{\"%s\":{\"%s\":\"%s\"}}";
    private static final String SERIALIZED = format(SERIALIZATION_FORMAT, APPLICATION_SERVER_ID, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
    private static final String CONFIGURATION_WITHOUT_APPLICATION_SERVER_ID
        = format(SERIALIZATION_FORMAT, "unknownApplicationServerId", ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

    @Override
    public ServerConfigurationReader newServerConfigurationReader(String serialized) {
        return new MultiServerConfigurationReader(APPLICATION_SERVER_ID, () -> serialized);
    }

    @Override
    public String getSerialized() {
        return SERIALIZED;
    }

    @Test
    void readEnvironmentConfigurationAttributeIfApplicationServerIdCannotBeFound() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(CONFIGURATION_WITHOUT_APPLICATION_SERVER_ID);

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
