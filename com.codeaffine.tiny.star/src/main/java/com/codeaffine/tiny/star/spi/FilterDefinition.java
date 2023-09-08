/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

import jakarta.servlet.Filter;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Objects;

import static com.codeaffine.tiny.shared.Reflections.newInstance;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;

/**
 * <p>Defines a {@link Filter} to be registered with the {@link jakarta.servlet.ServletContext} of the underlying web container.</p>
 */
@Value(staticConstructor = "of")
public class FilterDefinition {

    @NonNull
    String filterName;
    @NonNull
    Filter filter;
    @NonNull
    List<String> urlPatterns;

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} class. The filter name is derived from the given class and
     * the filter will handle all entry points of the web application.</p>
     *
     * @param filterClass the {@link Filter} class to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @return a {@link FilterDefinition} for the given {@link Filter} class
     */
    public static FilterDefinition of(@NonNull Class<? extends Filter> filterClass) {
        return of(filterClass.getName(), filterClass, emptyList());
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter}. The filter name is derived from the given filter's class name and
     * the filter will handle all entry points of the web application.</p>
     *
     * @param filter the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @return a {@link FilterDefinition} for the given {@link Filter}
     */
    public static FilterDefinition of(@NonNull Filter filter) {
        return of(filter.getClass().getName(), filter, emptyList());
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} class and filter name. Note that filter names should be unique.
     * The filter will handle all entry points of the web application.</p>
     *
     * @param filterName the name of the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null} and unique.
     * @param filterClass the {@link Filter} class to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @return a {@link FilterDefinition} for the given {@link Filter} class and filter name
     */
    public static FilterDefinition of(@NonNull String filterName, @NonNull Class<? extends Filter> filterClass) {
        return of(filterName, filterClass, emptyList());
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} and filter name. Note that filter names should be unique.
     * The filter will handle all entry points of the web application.</p>
     *
     * @param filterName the name of the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null} and unique.
     * @param filter the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @return a {@link FilterDefinition} for the given {@link Filter} and filter name
     */
    public static FilterDefinition of(@NonNull String filterName, @NonNull Filter filter) {
        return of(filterName, filter, emptyList());
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} class and url patterns. The filter name is derived from the given filter's class name.
     * The filter will handle all requests to the web application matching the given url patterns.</p>
     *
     * @param filterClass the {@link Filter} class to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @param urlPatterns the URL patterns to register the {@link Filter} for. Must not be {@code null}. Url patterns match the conventions specified
     *                    in the Servlet specification.
     * @return a {@link FilterDefinition} for the given {@link Filter} class and URL patterns
     */
    public static FilterDefinition of(@NonNull Class<? extends Filter> filterClass, @NonNull List<String> urlPatterns) {
        return of(filterClass.getName(), filterClass, urlPatterns);
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} and url patterns. The filter name is derived from the given filter's class name.
     * The filter will handle all requests to the web application matching the given url patterns.</p>
     *
     * @param filter the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @param urlPatterns the URL patterns to register the {@link Filter} for. Must not be {@code null}. Url patterns match the conventions specified
     *                    in the Servlet specification.
     * @return a {@link FilterDefinition} for the given {@link Filter} and URL patterns
     */
    public static FilterDefinition of(@NonNull Filter filter, @NonNull List<String> urlPatterns) {
        return of(filter.getClass().getName(), filter, urlPatterns);
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} class and url patterns. The filter name is derived from the given filter's class name.
     * The filter will handle all requests to the web application matching the given url patterns.</p>
     *
     * @param filterClass the {@link Filter} class to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @param urlPatterns the URL patterns to register the {@link Filter} for. Must not be {@code null}. Url patterns match the conventions specified
     *                    in the Servlet specification.
     * @return a {@link FilterDefinition} for the given {@link Filter} class and URL patterns
     */
    public static FilterDefinition of(@NonNull Class<? extends Filter> filterClass, String ... urlPatterns) {
        return of(filterClass.getName(), newInstance(filterClass), convertToList(urlPatterns));
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} and url patterns. The filter name is derived from the given filter's class name.
     * The filter will handle all requests to the web application matching the given url patterns.</p>
     *
     * @param filter the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @param urlPatterns the URL patterns to register the {@link Filter} for. Must not be {@code null}. Url patterns match the conventions specified
     *                    in the Servlet specification.
     * @return a {@link FilterDefinition} for the given {@link Filter} and URL patterns
     */
    public static FilterDefinition of(@NonNull Filter filter, String ... urlPatterns) {
        return of(filter.getClass().getName(), filter, convertToList(urlPatterns));
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} class, filter name and url patterns. Note that filter names should be unique.
     * The filter will handle all requests to the web application matching the given url patterns.</p>
     *
     * @param filterName the name of the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null} and unique.
     * @param filterClass the {@link Filter} class to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @param urlPatterns the URL patterns to register the {@link Filter} for. Must not be {@code null}. Url patterns match the conventions specified
     *                    in the Servlet specification.
     * @return a {@link FilterDefinition} for the given {@link Filter} class, filter name and URL patterns
     */
    public static FilterDefinition of(@NonNull String filterName, @NonNull Class<? extends Filter> filterClass, @NonNull List<String> urlPatterns) {
        return of(filterName, newInstance(filterClass), urlPatterns);
    }

    /**
     * <p>Create a {@link FilterDefinition} for the given {@link Filter} and filter name and url patterns. Note that filter names should be unique.
     * The filter will handle all requests to the web application matching the given url patterns.</p>
     *
     * @param filterName the name of the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null} and unique.
     * @param filter the {@link Filter} to create a {@link FilterDefinition} for. Must not be {@code null}.
     * @param urlPatterns the URL patterns to register the {@link Filter} for. Must not be {@code null}. Url patterns match the conventions specified
     *                    in the Servlet specification.
     * @return a {@link FilterDefinition} for the given {@link Filter}, filter name and URL patterns
     */
    public static FilterDefinition of(@NonNull String filterName, @NonNull Filter filter, String ... urlPatterns) {
        return of(filterName, filter, convertToList(urlPatterns));
    }

    private static List<String> convertToList(String[] urlPatterns) {
        return stream(urlPatterns)
            .filter(Objects::nonNull)
            .toList();
    }
}
