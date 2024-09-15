import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class BomPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("bom", BomExtension.class);
    }

    public static class BomExtension {

        void exclude(String bomName, String ... projectNames) throws IOException {
            for (String projectName : projectNames) {
                updateBomProperties(bomName, properties -> properties.setProperty(projectName, "false"));
            }
        }

        void include(String bomName, String ... projectNames) throws IOException {
            for (String projectName : projectNames) {
                updateBomProperties(bomName, properties -> properties.setProperty(projectName, "true"));
            }
        }

        Set<String> getBomProjectNames(String bomName) throws IOException {
            File bomDefinition = getBomDefinitionFile(bomName);
            Properties properties = loadProperties(bomDefinition);
            return properties
                .entrySet()
                .stream()
                .filter(entry -> "true".equalsIgnoreCase((String)entry.getValue()))
                .map(entry -> (String)entry.getKey())
                .collect(Collectors.toSet());
        }

        private void updateBomProperties(String bomName, Consumer<Properties> updateAction) throws IOException {
            File bomDefinition = getBomDefinitionFile(bomName);
            if (!bomDefinition.exists()) {
                boolean success = bomDefinition.createNewFile();
                if (!success) {
                    throw new IOException("Failed to create file " + bomDefinition);
                }
            }
            Properties properties = loadProperties(bomDefinition);
            updateAction.accept(properties);
            properties.store(new FileOutputStream(bomDefinition), null);
        }

        private static File getBomDefinitionFile(String bomName) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            return new File(tmpDir, bomName);
        }

        private static Properties loadProperties(File bomDefinition) throws IOException {
            Properties properties = new Properties();
            properties.load(new FileInputStream(bomDefinition));
            return properties;
        }
    }
}
