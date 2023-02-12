package com.codeaffine.tiny.star.tck;

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
