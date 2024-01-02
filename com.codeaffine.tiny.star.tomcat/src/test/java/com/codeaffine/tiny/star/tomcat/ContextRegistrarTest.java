/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.stubServerConfiguration;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContextRegistrarTest {

    @TempDir
    private File workingDirectory;
    private Tomcat tomcat;

    @BeforeEach
    void setUp() {
        tomcat = spy(new Tomcat());
        workingDirectory.deleteOnExit(); // @TempDir(cleanup = ALWAYS) has no effect.
    }

    @Test
    void addContext() {
        ContextRegistrar registrar = new ContextRegistrar(stubServerConfiguration(workingDirectory), tomcat);

        Context actual = registrar.addContext();

        assertThat(actual.getPath()).isEqualTo(ContextRegistrar.CONTEXT_PATH);
        assertThat(actual.getDocBase()).isEqualTo(expectedDocBasePath());
        assertThat(new File(actual.getDocBase())).exists();
        assertThat(actual.getSessionTimeout()).isEqualTo(DEFAULT_SESSION_TIMEOUT);
            verify(tomcat).setBaseDir(workingDirectory.getAbsolutePath());
    }

    @Test
    void addContextIfDocBaseAlreadyExists() {
        boolean successCreatingDocBase = expectedDocBase().mkdirs();
        ContextRegistrar registrar = new ContextRegistrar(stubServerConfiguration(workingDirectory), tomcat);

        Context actual = registrar.addContext();

        assertThat(successCreatingDocBase).isTrue();
        assertThat(actual.getPath()).isEqualTo(ContextRegistrar.CONTEXT_PATH);
        assertThat(actual.getDocBase()).isEqualTo(expectedDocBasePath());
        assertThat(new File(actual.getDocBase())).exists();
        verify(tomcat).setBaseDir(workingDirectory.getAbsolutePath());
    }

    @Test
    void addContextIfDocBaseCannotBeCreated() {
        File notExistingWorkingDirectory = new File("notExisting");
        ContextRegistrar registrar = new ContextRegistrar(stubServerConfiguration(notExistingWorkingDirectory), tomcat);

        Exception actual = catchException(registrar::addContext);

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(new File(notExistingWorkingDirectory, ContextRegistrar.DOC_BASE).getAbsolutePath());
        verify(tomcat, never()).setBaseDir(workingDirectory.getAbsolutePath());
    }

    @Test
    void constructWithNullAsWorkingDirectoryArgument() {
        assertThatThrownBy(() -> new ContextRegistrar(null, tomcat))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsTomcatArgument() {
        ServerConfiguration configuration = stubServerConfiguration(workingDirectory);

        assertThatThrownBy(() -> new ContextRegistrar(configuration, null))
            .isInstanceOf(NullPointerException.class);
    }

    private String expectedDocBasePath() {
        return expectedDocBase().getAbsolutePath();
    }

    private File expectedDocBase() {
        return new File(workingDirectory, ContextRegistrar.DOC_BASE);
    }
}
