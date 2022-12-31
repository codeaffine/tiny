package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import io.undertow.servlet.api.ServletInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.util.Set;

import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static io.undertow.servlet.Servlets.servlet;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ServletInfoFactory {

    static final String SERVLET_NAME = "rwtServlet";
    static final String ALL_SUB_PATHS_PATTERN = "/*";

    @NonNull
    private final ApplicationConfiguration applicationConfiguration;

    ServletInfo createRwtServletInfo() {
        ServletInfo result = servlet(SERVLET_NAME, RwtServletAdapter.class);
        Set<String> entrypointPaths = captureEntrypointPaths(applicationConfiguration);
        entrypointPaths.forEach(path -> result.addMapping(path + ALL_SUB_PATHS_PATTERN));
        return result;
    }
}
