/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.log4j;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String ERROR_UNABLE_TO_CONFIGURE_LOG4J2 = "Unable to configure log4j2";
    static final String LOG_LOG4J2_DETECTED = "Seems that Log4j2 is used for logging. Try to reconfigure it to respect settings declared in {}.";
    static final String LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_CONTEXT_CLASS_LOADER = "Trying to load log4j2 configuration {} from context class loader.";
    static final String LOG_TRY_LOADING_CONFIGURATION_FROM_APPLICATION_CLASS_LOADER = "Trying to load log4j2 configuration {} from application class loader.";
    static final String LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_SYSTEM_CLASS_LOADER = "Trying to load log4j2 configuration {} from system class loader.";
    static final String LOG_LOG4J2_CONFIGURATION_NOT_FOUND = "Unable to find Log4j2 configuration {} so keep current configuration.";
    static final String LOG_LOG4J2_RECONFIGURATION_SUCCESSFUL = "Log4j2 reconfiguration to {} successfully finished.";
}
