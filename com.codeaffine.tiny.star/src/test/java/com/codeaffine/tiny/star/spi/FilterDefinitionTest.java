/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

import jakarta.servlet.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterDefinitionTest {

    private static final String FILTER_NAME = "filterName";
    private static final String URL_PATTER_1 = "/path1/*";
    private static final String URL_PATTER_2 = "/path2*";

    static class TestFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        }
    }

    @Test
    void fullFactory() {
        TestFilter expectedFilter = new TestFilter();

        FilterDefinition actual = FilterDefinition.of(FILTER_NAME, expectedFilter, List.of(URL_PATTER_1, URL_PATTER_2));

        assertThat(actual.getFilter()).isEqualTo(expectedFilter);
        assertThat(actual.getFilterName()).isEqualTo(FILTER_NAME);
        assertThat(actual.getUrlPatterns()).containsExactly(URL_PATTER_1, URL_PATTER_2);
    }

    @Test
    void fullFactoryWithFilterClassName() {
        FilterDefinition actual = FilterDefinition.of(FILTER_NAME, TestFilter.class, List.of(URL_PATTER_1, URL_PATTER_2));

        assertThat(actual.getFilter()).isInstanceOf(TestFilter.class);
        assertThat(actual.getFilterName()).isEqualTo(FILTER_NAME);
        assertThat(actual.getUrlPatterns()).containsExactly(URL_PATTER_1, URL_PATTER_2);
    }

    @Test
    void factoryWithFilterClassNameOnly() {
        FilterDefinition actual = FilterDefinition.of(TestFilter.class);

        assertThat(actual.getFilter()).isInstanceOf(TestFilter.class);
        assertThat(actual.getFilterName()).isEqualTo(TestFilter.class.getName());
        assertThat(actual.getUrlPatterns()).isEmpty();
    }

    @Test
    void factoryWithFilterInstanceOnly() {
        TestFilter expectedFilter = new TestFilter();

        FilterDefinition actual = FilterDefinition.of(expectedFilter);

        assertThat(actual.getFilter()).isEqualTo(expectedFilter);
        assertThat(actual.getFilterName()).isEqualTo(TestFilter.class.getName());
        assertThat(actual.getUrlPatterns()).isEmpty();
    }

    @Test
    void factoryWithFilterClassNameAndFilterName() {
        FilterDefinition actual = FilterDefinition.of(FILTER_NAME, TestFilter.class);

        assertThat(actual.getFilter()).isInstanceOf(TestFilter.class);
        assertThat(actual.getFilterName()).isEqualTo(FILTER_NAME);
        assertThat(actual.getUrlPatterns()).isEmpty();
    }

    @Test
    void factoryWithFilterInstanceAndFilterName() {
        TestFilter expectedFilter = new TestFilter();

        FilterDefinition actual = FilterDefinition.of(FILTER_NAME, expectedFilter);

        assertThat(actual.getFilter()).isEqualTo(expectedFilter);
        assertThat(actual.getFilterName()).isEqualTo(FILTER_NAME);
        assertThat(actual.getUrlPatterns()).isEmpty();
    }

    @Test
    void factoryWithFilterClassNameAndUrlPatterns() {
        FilterDefinition actual = FilterDefinition.of(TestFilter.class, List.of(URL_PATTER_1, URL_PATTER_2));

        assertThat(actual.getFilter()).isInstanceOf(TestFilter.class);
        assertThat(actual.getFilterName()).isEqualTo(TestFilter.class.getName());
        assertThat(actual.getUrlPatterns()).containsExactly(URL_PATTER_1, URL_PATTER_2);
    }

    @Test
    void factoryWithFilterInstanceAndUrlPatterns() {
        TestFilter expectedFilter = new TestFilter();

        FilterDefinition actual = FilterDefinition.of(expectedFilter, List.of(URL_PATTER_1, URL_PATTER_2));

        assertThat(actual.getFilter()).isEqualTo(expectedFilter);
        assertThat(actual.getFilterName()).isEqualTo(TestFilter.class.getName());
        assertThat(actual.getUrlPatterns()).containsExactly(URL_PATTER_1, URL_PATTER_2);
    }

    @Test
    void factoryWithFilterClassNameAndUrlPatternsAsVarargs() {
        FilterDefinition actual = FilterDefinition.of(TestFilter.class, URL_PATTER_1, URL_PATTER_2);

        assertThat(actual.getFilter()).isInstanceOf(TestFilter.class);
        assertThat(actual.getFilterName()).isEqualTo(TestFilter.class.getName());
        assertThat(actual.getUrlPatterns()).containsExactly(URL_PATTER_1, URL_PATTER_2);
    }

    @Test
    void factoryWithFilterInstanceAndUrlPatternsAsVarargs() {
        TestFilter expectedFilter = new TestFilter();

        FilterDefinition actual = FilterDefinition.of(expectedFilter, URL_PATTER_1, URL_PATTER_2);

        assertThat(actual.getFilter()).isEqualTo(expectedFilter);
        assertThat(actual.getFilterName()).isEqualTo(TestFilter.class.getName());
        assertThat(actual.getUrlPatterns()).containsExactly(URL_PATTER_1, URL_PATTER_2);
    }

    @Test
    void factoryWithNullAsFilterNameArgument() {
        TestFilter filter = new TestFilter();
        List<String> urlPatterns = List.of(URL_PATTER_1, URL_PATTER_2);

        assertThatThrownBy(() -> FilterDefinition.of(null, filter, urlPatterns))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void factoryWithNullAsFilterArgument() {
        List<String> urlPatterns = List.of(URL_PATTER_1, URL_PATTER_2);

        assertThatThrownBy(() -> FilterDefinition.of(FILTER_NAME, (Filter) null, urlPatterns))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void factoryWithNullAsFilterClassArgument() {
        List<String> urlPatterns = List.of(URL_PATTER_1, URL_PATTER_2);

        assertThatThrownBy(() -> FilterDefinition.of((Class<? extends Filter>) null, urlPatterns))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void factoryWithNullAsUrlPatternsArgument() {
        TestFilter filter = new TestFilter();

        assertThatThrownBy(() -> FilterDefinition.of(FILTER_NAME, filter, (List<String>) null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void factoryWithNullAsUrlPatternsArgumentAsVarargs() {
        TestFilter filter = new TestFilter();

        assertThatThrownBy(() -> FilterDefinition.of(FILTER_NAME, filter, (String[]) null))
            .isInstanceOf(NullPointerException.class);
    }
}
