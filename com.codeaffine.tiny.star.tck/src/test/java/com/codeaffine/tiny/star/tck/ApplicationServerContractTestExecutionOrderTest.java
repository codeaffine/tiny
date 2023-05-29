/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import org.junit.jupiter.api.MethodDescriptor;
import org.junit.jupiter.api.MethodOrdererContext;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationServerContractTestExecutionOrderTest {

    @Test
    void orderMethods() {
        ApplicationServerContractTestExecutionOrder executionOrder = new ApplicationServerContractTestExecutionOrder();
        MethodDescriptor startDescriptor = stubMethodDescriptorWithAnnotation(StartApplicationServer.class);
        MethodDescriptor stopDescriptor = stubMethodDescriptorWithAnnotation(StopApplicationServer.class);
        MethodDescriptor generalMethodDescriptor1 = mock(MethodDescriptor.class);
        MethodDescriptor generalMethodDescriptor2 = mock(MethodDescriptor.class);
        MethodOrdererContext context = stubMethodOrderContext(stopDescriptor, startDescriptor, generalMethodDescriptor1, generalMethodDescriptor2);

        List<MethodDescriptor> before = captureMethodDescriptors(context);
        executionOrder.orderMethods(context);
        List<MethodDescriptor> after = captureMethodDescriptors(context);

        assertThat(before).containsExactly(stopDescriptor, startDescriptor, generalMethodDescriptor1, generalMethodDescriptor2);
        assertThat(after).containsExactly(startDescriptor, generalMethodDescriptor1, generalMethodDescriptor2, stopDescriptor);
    }

    private static MethodDescriptor stubMethodDescriptorWithAnnotation(Class<? extends Annotation> annotationType) {
        MethodDescriptor result = mock(MethodDescriptor.class);
        when(result.isAnnotated(annotationType)).thenReturn(true);
        return result;
    }

    private static MethodOrdererContext stubMethodOrderContext(MethodDescriptor ... descriptors) {
        MethodOrdererContext result = mock(MethodOrdererContext.class);
        List<MethodDescriptor> descriptorList = new ArrayList<>(List.of(descriptors));
        when(result.getMethodDescriptors()).thenAnswer(invocation -> descriptorList);
        return result;
    }

    private static List<MethodDescriptor> captureMethodDescriptors(MethodOrdererContext context) {
        return context.getMethodDescriptors()
            .stream()
            .map(descriptor -> (MethodDescriptor) descriptor)
            .toList();
    }
}
