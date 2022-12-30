package com.codeaffine.tiny.star.tomcat;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

import static java.lang.String.format;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ContextRegistrar {

    static final String CONTEXT_PATH = "";
    static final String DOC_BASE = "doc-base";

    @NonNull
    private final File workingDirectory;
    @NonNull
    private final Tomcat tomcat;

    Context addContext() {
        File docBase = new File(workingDirectory, DOC_BASE);
        if (!docBase.exists() && !docBase.mkdir()) {
            throw new IllegalStateException(format(Texts.ERROR_CREATE_DOC_BASE, docBase.getAbsolutePath()));
        }
        tomcat.setBaseDir(workingDirectory.getAbsolutePath());
        return tomcat.addContext(CONTEXT_PATH, docBase.getAbsolutePath());
    }
}
