/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared.test.test.fixtures;

import com.codeaffine.tiny.shared.ServiceLoaderAdapter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Helper class to create a fake {@link ServiceLoaderAdapter} for testing purposes.
 */
@NoArgsConstructor(access = PRIVATE)
public class ServiceLoaderAdapterTestHelper {

    /**
     * Creates a fake {@link ServiceLoaderAdapter} that returns the provided service implementations
     * when {@link ServiceLoaderAdapter#collectServiceTypeImplementations()} is called.
     * No service registration is needed nor any service loader is used. It is just a stub
     * for testing purposes.
     *
     * @param serviceTypeImplementations the service implementation types to be returned
     * @param <T>      the service type
     * @return a {@link ServiceLoaderAdapter} stub
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceLoaderAdapter<T> stubServiceLoaderAdapter(T... serviceTypeImplementations) {
        ServiceLoaderAdapter<T> result = mock(ServiceLoaderAdapter.class);
        when(result.collectServiceTypeImplementations()).thenReturn(new ArrayList<>(List.of(serviceTypeImplementations)));
        when(result.collectServiceTypeImplementationClassNames()).thenReturn(collectClassNamesOf(serviceTypeImplementations));
        return result;
    }

    private static <T> String collectClassNamesOf(T[] factories) {
        return Stream.of(factories)
            .map(Object::getClass)
            .map(Class::getName)
            .collect(joining(", "));
    }
}
