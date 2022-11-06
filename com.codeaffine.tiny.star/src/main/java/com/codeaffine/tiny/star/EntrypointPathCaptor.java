package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.application.ExceptionHandler;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rap.rwt.service.SettingStoreFactory;
import org.eclipse.swt.widgets.Widget;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class EntrypointPathCaptor {

    @RequiredArgsConstructor(access = PACKAGE)
    static class PathCaptor implements Application {

        @NonNull
        private final Set<String> paths;

        @Override
        public void setOperationMode(OperationMode operationMode) {
            // do nothing
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
            // do nothing
        }

        @Override
        public void addStyleSheet(String themeId, String styleSheetLocation, ResourceLoader resourceLoader) {
            // do nothing
        }

        @Override
        public void setAttribute(String name, Object value) {
            // do nothing
        }

        @Override
        public void setSettingStoreFactory(SettingStoreFactory settingStoreFactory) {
            // do nothing
        }

        @Override
        public void setExceptionHandler(ExceptionHandler exceptionHandler) {
            // do nothing
        }

        @Override
        public void addThemeableWidget(Class<? extends Widget> widget) {
            // do nothing
        }

        @Override
        public void addServiceHandler(String serviceHandlerId, ServiceHandler serviceHandler) {
            // do nothing
        }

        @Override
        public void addResource(String resourceName, ResourceLoader resourceLoader) {
            // do nothing
        }
    }

    public static Set<String> captureEntrypointPaths(ApplicationConfiguration applicationConfiguration) {
        Set<String> result = new HashSet<>();
        applicationConfiguration.configure(new PathCaptor(result));
        return result;
    }
}
