package com.codeaffine.tiny.star;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.application.*;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rap.rwt.service.SettingStoreFactory;
import org.eclipse.swt.widgets.Widget;

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

        @Override
        public void setOperationMode(OperationMode operationMode) {
            // not needed yet
        }

        @Override
        public void addEntryPoint(String path, Class<? extends EntryPoint> entryPointType, Map<String, String> properties) {
            paths.add(path);
        }

        @Override
        public void addEntryPoint(String path, EntryPointFactory entryPointFactory, Map<String, String> properties) {
            paths.add(path);
        }

        @Override
        public void addStyleSheet(String themeId, String styleSheetLocation) {
            // not needed yet
        }

        @Override
        public void addStyleSheet(String themeId, String styleSheetLocation, ResourceLoader resourceLoader) {
            // not needed yet
        }

        @Override
        public void setAttribute(String name, Object value) {
            // not needed yet
        }

        @Override
        public void setSettingStoreFactory(SettingStoreFactory settingStoreFactory) {
            // not needed yet
        }

        @Override
        public void setExceptionHandler(ExceptionHandler exceptionHandler) {
            // not needed yet
        }

        @Override
        public void addThemeableWidget(Class<? extends Widget> widget) {
            // not needed yet
        }

        @Override
        public void addServiceHandler(String serviceHandlerId, ServiceHandler serviceHandler) {
            // not needed yet
        }

        @Override
        public void addResource(String resourceName, ResourceLoader resourceLoader) {
            // not needed yet
        }
    }

    public static Set<String> captureEntrypointPaths(ApplicationConfiguration applicationConfiguration) {
        Set<String> result = new HashSet<>();
        applicationConfiguration.configure(new PathCaptor(result));
        return result;
    }
}
