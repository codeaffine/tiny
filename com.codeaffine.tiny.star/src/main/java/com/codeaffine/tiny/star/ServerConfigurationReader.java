/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import lombok.NonNull;

import java.util.function.Function;

interface ServerConfigurationReader {

    String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = "com.codeaffine.tiny.star.configuration";

    <T> T readEnvironmentConfigurationAttribute(@NonNull String attributeName, T defaultValue, @NonNull Class<T> type);
    <T> T readEnvironmentConfigurationAttribute(@NonNull String attributeName, T defaultValue, @NonNull Function<String, T> factory);

    SecureSocketLayerConfiguration readSecureSocketLayerConfiguration();
}
