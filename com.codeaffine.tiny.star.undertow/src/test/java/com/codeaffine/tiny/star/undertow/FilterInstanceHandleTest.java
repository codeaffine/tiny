/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.FILTER_DEFINITION_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterInstanceHandleTest {

    @Test
    void getFilter() {
        FilterInstanceHandle filterInstanceHandle = new FilterInstanceHandle(FILTER_DEFINITION_1);

        Filter actual = filterInstanceHandle.getInstance();

        assertThat(actual).isSameAs(FILTER_DEFINITION_1.getFilter());
    }

    @Test
    void constructWithNullAsFilterDefinitionArgument() {
        assertThatThrownBy(() -> new FilterInstanceHandle(null))
            .isInstanceOf(NullPointerException.class);
    }
}
