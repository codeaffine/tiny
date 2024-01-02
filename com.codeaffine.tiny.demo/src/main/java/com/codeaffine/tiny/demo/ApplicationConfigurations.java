/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.Application;

class ApplicationConfigurations {

    private ApplicationConfigurations() {
        // prevent instantiation
    }

    static void configureApplication(Application application, Class<? extends AbstractEntryPoint> entryPointClass) {
        application.addEntryPoint("/" + entryPointClass.getSimpleName().toLowerCase(), entryPointClass, null);
    }
}
