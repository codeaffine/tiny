# Tiny Project Development Guidelines

This document provides essential information for developers working on the Tiny project.

## Build/Configuration Instructions

### Project Structure

The Tiny project is a multi-module Gradle project with the following key modules:

- `com.codeaffine.tiny.star`: Core module
- `com.codeaffine.tiny.star.cli`: Command-line interface module
- `com.codeaffine.tiny.star.tomcat`: Tomcat integration
- `com.codeaffine.tiny.star.undertow`: Undertow integration
- `com.codeaffine.tiny.star.log4j`: Log4j integration
- `com.codeaffine.tiny.star.logback`: Logback integration
- `com.codeaffine.tiny.test`: Testing utilities
- `com.codeaffine.tiny.bom`: Bill of Materials (BOM) definitions

### Build System

The project uses Gradle with custom plugins defined in the `com.codeaffine.tiny.build` directory:

- `com.codeaffine.tiny.settings`: Configures project structure and repositories
- `com.codeaffine.tiny.root`: Configures root project with version management and main build tasks
- `com.codeaffine.tiny.java.modules`: Configures Java modules
- `com.codeaffine.tiny.java.modules.bom.integration-test`: Configures BOM integration tests

### Building the Project

To build the entire project:

```bash
./gradlew build
```

To clean the project:

```bash
./gradlew clean
```

To generate Javadoc:

```bash
./gradlew javadoc
```

## Testing Information

### Test Structure

The project uses JUnit 5 for testing with the following test types:

1. **Unit Tests**: Located in `src/test/java` directories
2. **Integration Tests**: Located in `src/integrationTest/java` directories (for BOM modules)
3. **Test Fixtures**: Located in `src/testFixtures/java` directories, providing reusable test components

### Running Tests

To run all tests:

```bash
./gradlew test
```

To run integration tests:

```bash
./gradlew integrationTest
```

To run tests for a specific module:

```bash
./gradlew :module-name:test
```

For example:

```bash
./gradlew :com.codeaffine.tiny.test:test
```

### Creating Tests

1. Create test classes in the appropriate `src/test/java` directory
2. Use the JUnit 5 `@Test` annotation for test methods
3. Use AssertJ for assertions (`assertThat()`)
4. Follow the setup, execute, and verify test phases (also known as build-operate-check pattern)
5. Use empty lines to separate test phases, not comments
6. Avoid using comments or empty lines for structuring Java code as this indicates the need for method extraction

Example:

```java
package com.codeaffine.tiny.test;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleTest {
    @Test
    void testSimpleAssertion() {
        String text = "Hello, World!";

        String result = text.toUpperCase();

        assertThat(result).isEqualTo("HELLO, WORLD!");
    }
}
```

### Contract-Based Testing

The project uses contract-based testing for BOM modules:

1. **BomUiStartContract**: Tests that the application delivers an initial start page and widget tree
2. **BomWorkDirStructureContract**: Tests that the application working directory has the expected structure

To implement a contract test:

```java
import com.codeaffine.tiny.star.test.fixtures.BomUiStartContract;
import com.codeaffine.tiny.star.test.fixtures.BomWorkDirStructureContract;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest implements BomUiStartContract, BomWorkDirStructureContract {
    @Override
    public void verifyStructure(Path workingDirectory) throws Exception {
        assertThat(workingDirectory.resolve("doc-base")).isNotEmptyDirectory();
        assertThat(workingDirectory.resolve("logs").resolve("application.log")).isRegularFile();
        assertThat(workingDirectory.resolve("work")).isNotEmptyDirectory();
    }
}
```

## Additional Development Information

### Code Style

- The project uses Java modules (JPMS)
- Follow standard Java naming conventions
- Use AssertJ for assertions in tests
- Structure tests using the setup, execute, and verify test phases (build-operate-check pattern)

### Dependency Management

Dependencies are managed through a version catalog in `com.codeaffine.tiny.repositories.settings.gradle`.

### Integration Testing

BOM integration tests:

1. Build a Maven-based test application
2. Run the application
3. Execute integration tests against the running application
4. Stop the application

The integration test configuration is in `com.codeaffine.tiny.java.modules.bom.integration-test.gradle`.

### Test Fixtures

The project provides test fixtures for:

1. **Logging**: `com.codeaffine.tiny.test.test.fixtures.logging`
2. **System I/O**: `com.codeaffine.tiny.test.test.fixtures.system.io`

These can be used to simplify testing across the project.
