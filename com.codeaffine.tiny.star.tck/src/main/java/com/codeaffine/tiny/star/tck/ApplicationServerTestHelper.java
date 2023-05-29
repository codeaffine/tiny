/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import lombok.NoArgsConstructor;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("java:S1075")
public class ApplicationServerTestHelper {

    public static final String ENTRYPOINT_PATH_1 = "/ep1";
    public static final String ENTRYPOINT_PATH_2 = "/ep2";
    public static final ApplicationConfiguration MULTI_ENTRYPOINT_CONFIGURATION = application -> {
        application.addEntryPoint(ENTRYPOINT_PATH_1, () -> null, null);
        application.addEntryPoint(ENTRYPOINT_PATH_2, () -> null, null);
    };
}
