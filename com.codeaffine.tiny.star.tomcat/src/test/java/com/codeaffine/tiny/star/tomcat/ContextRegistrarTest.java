package com.codeaffine.tiny.star.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static org.mockito.Mockito.*;

class ContextRegistrarTest {

    @TempDir(cleanup = ALWAYS)
    private File workingDirectory;
    private Tomcat tomcat;

    @BeforeEach
    void setUp() {
        tomcat = spy(new Tomcat());
    }

    @Test
    void addContext() {
        ContextRegistrar registrar = new ContextRegistrar(workingDirectory, tomcat);

        Context actual = registrar.addContext();

        assertThat(actual.getPath()).isEqualTo(ContextRegistrar.CONTEXT_PATH);
        assertThat(actual.getDocBase()).isEqualTo(expectedDocBasePath());
        assertThat(new File(actual.getDocBase())).exists();
        verify(tomcat).setBaseDir(workingDirectory.getAbsolutePath());
    }

    @Test
    void addContextIfDocBaseAlreadyExists() {
        boolean successCreatingDocBase = expectedDocBase().mkdirs();
        ContextRegistrar registrar = new ContextRegistrar(workingDirectory, tomcat);

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
        ContextRegistrar registrar = new ContextRegistrar(notExistingWorkingDirectory, tomcat);

        Throwable actual = catchThrowable(registrar::addContext);

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
        assertThatThrownBy(() -> new ContextRegistrar(workingDirectory, null))
            .isInstanceOf(NullPointerException.class);
    }

    private String expectedDocBasePath() {
        return expectedDocBase().getAbsolutePath();
    }

    private File expectedDocBase() {
        return new File(workingDirectory, ContextRegistrar.DOC_BASE);
    }
}
