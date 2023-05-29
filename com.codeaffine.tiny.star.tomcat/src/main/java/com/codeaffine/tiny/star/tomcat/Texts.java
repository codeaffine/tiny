/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String ERROR_CREATE_DOC_BASE = "Could not create doc base: %s.";
    static final String ERROR_STARTING_TOMCAT = "Unable to start embedded tomcat.";
    static final String ERROR_STOPPING_TOMCAT = "Unable to stop embedded tomcat.";
    static final String SERVER_NAME = "Tomcat";
}
