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
