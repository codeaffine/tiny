/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String ERROR_UNABLE_TO_DETECT_KEYSTORE_TYPE = "Unable to detect keystore type. Supported types are JKS and PKCS12.";
    static final String ERROR_INVALID_KEY_STORE = "Invalid key store.";
    static final String ERROR_INVALID_KEY_STORE_PASSWORD = "Unable to authorize access to key store.";
    static final String ERROR_INVALID_KEY_PASSWORD = "Unable to authorize access to key.";
    static final String ERROR_ALIAS_NOT_FOUND = "Alias '%s' not found in key store.";
    static final String ERROR_VERIFYING_ALIAS = "Error verifying alias '%s' in key store.";
    static final String ERROR_READING_KEY_STORE = "Error reading key store.";
}
