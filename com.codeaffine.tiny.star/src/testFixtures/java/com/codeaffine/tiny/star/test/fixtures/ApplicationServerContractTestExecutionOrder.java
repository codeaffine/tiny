/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.test.fixtures;

import org.junit.jupiter.api.MethodDescriptor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrdererContext;

public class ApplicationServerContractTestExecutionOrder implements MethodOrderer {

    @Override
    public void orderMethods(MethodOrdererContext context) {
        context.getMethodDescriptors().sort(ApplicationServerContractTestExecutionOrder::sort);
    }

    private static int sort(MethodDescriptor method1, MethodDescriptor method2) {
        if (method1.isAnnotated(StartApplicationServer.class) || method2.isAnnotated(StopApplicationServer.class)) {
            return -1;
        }
        if (method1.isAnnotated(StopApplicationServer.class) || method2.isAnnotated(StartApplicationServer.class)) {
            return 1;
        }
        return 0;
    }
}
