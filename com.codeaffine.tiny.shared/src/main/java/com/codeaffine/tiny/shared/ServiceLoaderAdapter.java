/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public class ServiceLoaderAdapter<T> {

    @NonNull
    private final Class<T> serviceTypeFactory;
    @NonNull
    private final Function<Class<T>, ServiceLoader<T>> serviceLoaderFunction;

    public List<T> collectServiceTypeFactories() {
        List<T> result = new ArrayList<>();
        for (T factory : serviceLoaderFunction.apply(serviceTypeFactory)) {
            result.add(factory);
        }
        return result;
    }

    public String collectServiceTypeFactoryClassNames() {
        return collectServiceTypeFactories()
            .stream()
            .map(factory -> factory.getClass().getName())
            .collect(joining(","));
    }
}
