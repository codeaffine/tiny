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

@NoArgsConstructor(access = PRIVATE)
public class ServiceLoaderAdapterTestHelper {

    @SuppressWarnings("unchecked")
    public static <T> ServiceLoaderAdapter<T> fakeServiceLoaderAdapter(T... factories) {
        ServiceLoaderAdapter<T> result = mock(ServiceLoaderAdapter.class);
        when(result.collectServiceTypeFactories()).thenReturn(new ArrayList<>(List.of(factories)));
        when(result.collectServiceTypeFactoryClassNames()).thenReturn(collectClassNamesOf(factories));
        return result;
    }

    private static <T> String collectClassNamesOf(T[] factories) {
        return Stream.of(factories)
            .map(Object::getClass)
            .map(Class::getName)
            .collect(joining(", "));
    }
}
