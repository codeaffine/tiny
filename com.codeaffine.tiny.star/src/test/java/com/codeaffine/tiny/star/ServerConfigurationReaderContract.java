/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

interface ServerConfigurationReaderContract {

    String ATTRIBUTE_NAME = "attribute";
    String ATTRIBUTE_VALUE = "value";
    String ATTRIBUTE_DEFAULT_VALUE = "defaultValue";
    String INVALID_SERIALISATION_FORMAT = "invalid";

    ServerConfigurationReader newServerConfigurationReader(String serialized);
    String getSerialized();

    @Test
    default void readEnvironmentConfigurationAttribute() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        String actual = configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeIfAttributeNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsDefaultValueArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", null, String.class);

        assertThat(actual).isNull();
    }

    @Test
    default void readEnvironmentConfigurationAttributeUsingFactoryArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        String actual = configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeUsingFactoryArgumentIfAttributeNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeUsingFactoryArgumentIfEnvironmentVariableIsNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsAttributeArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(null, ATTRIBUTE_DEFAULT_VALUE, String.class))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsAttributeArgumentUsingFactory() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(null, ATTRIBUTE_DEFAULT_VALUE, String::valueOf))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsFactoryArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        Function<String, String> factory = null;

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, factory))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsTypeArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getSerialized());

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, (Class<String>) null))
            .isInstanceOf(NullPointerException.class);
    }

}
