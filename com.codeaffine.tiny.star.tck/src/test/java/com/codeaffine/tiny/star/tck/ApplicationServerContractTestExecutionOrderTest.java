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
