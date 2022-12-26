package com.codeaffine.tiny.star;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.eclipse.rap.rwt.application.*;
import org.eclipse.rap.rwt.internal.application.ApplicationImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EntrypointPathCaptor {

    @RequiredArgsConstructor(access = PACKAGE)
    static class PathCaptor implements Application {

        @NonNull
        private final Set<String> paths;
        @Delegate(excludes = Excludes.class)
        private final Application application = new ApplicationImpl(null,  null);

        @SuppressWarnings("unused")
        interface Excludes {
            void addEntryPoint(String path, Class<? extends EntryPoint> entryPointType, Map<String, String> properties);
            void addEntryPoint(String path, EntryPointFactory entryPointFactory, Map<String, String> properties);
        }

        @Override
        public void addEntryPoint(String path, Class<? extends EntryPoint> entryPointType, Map<String, String> properties) {
            paths.add(path);
        }

        @Override
        public void addEntryPoint(String path, EntryPointFactory entryPointFactory, Map<String, String> properties) {
            paths.add(path);
        }
    }

    public static Set<String> captureEntrypointPaths(ApplicationConfiguration applicationConfiguration) {
        Set<String> result = new HashSet<>();
        applicationConfiguration.configure(new PathCaptor(result));
        return result;
    }
}
