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

/**
 * <p>ServiceLoaderAdapter is a utility class that simplifies the process of loading
 * service providers using the {@link ServiceLoader} mechanism. It supplies methods
 * to collect the available service type implementations and their class names.</p>
 * <p>It is designed to be used in conjunction with the Java module system. Java
 * modules declare service types they consume with the uses statement in their
 * module-info.java file. Such modules can use this class to load the appropriate
 * service type implementations that are declared as service providers
 * in the module-info.java file or in the META-INF/services directory by
 * other modules or by the module itself.</p>
 *
 * <p>Although, the {@link ServiceLoaderAdapter} does not much clue code reduction it
 * eases class structuring for testing purposes. Being a dependent-on-component it
 * can be injected into the class that needs to load the service type and can be
 * replaced by a stub at test time. Therefore, the accompanying test fixture provides a
 * helper class that can be used to stub the {@link ServiceLoaderAdapter}.</p>
 *
 * @param <S> the type of the service declared with an uses clause in the module-info.java
 *           file of the module using this class.
 */
@RequiredArgsConstructor
public class ServiceLoaderAdapter<S> {

    /**
     * @param serviceType the type of service to be loaded by this adapter. It is used to
     *                    load the service type implementation specified as service provider
     *                    in the module-info.java file or META-INF/services directory of java
     *                    modules.
     */
    @SuppressWarnings("JavadocDeclaration")
    @NonNull
    private final Class<S> serviceType;

    /**
     * @param serviceLoaderFunction this is one of the {@link ServiceLoader}.load method variants.
     */
    @SuppressWarnings("JavadocDeclaration")
    @NonNull
    private final Function<Class<S>, ServiceLoader<S>> serviceLoaderFunction;

    /**
     * Collects all service types of the specified service type.
     *
     * @return a list of service type implementations
     */
    public List<S> collectServiceTypeImplementations() {
        List<S> result = new ArrayList<>();
        for (S factory : serviceLoaderFunction.apply(serviceType)) {
            result.add(factory);
        }
        return result;
    }

    /**
     * Collects all service type implementation class names of the specified service type.
     *
     * @return a comma-separated string of service type implementation class names
     */
    public String collectServiceTypeImplementationClassNames() {
        return collectServiceTypeImplementations()
            .stream()
            .map(factory -> factory.getClass().getName())
            .collect(joining(","));
    }
}
