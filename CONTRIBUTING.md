# Contributing Guidelines

Thank you for your interest in contributing to this project!

This repository uses a modular Gradle build structure.  
Before submitting changes or creating new modules, please take note of the following guidelines.

---

## Project Layout

The project is organized as a multi-module Gradle build.

- Top-level modules reside directly in the project root.
- Thematically grouped modules may be placed in subfolders marked with a `.gradle-structure` file.
- These structural folders are not included as Gradle modules themselves.

See [CONVENTIONS.md](./CONVENTIONS.md#project-structure) for detailed information on layout and naming.

---

## Adding a New Module

1. Create a new folder (in root or under a structural folder).
2. Add a `build.gradle` file with the module’s configuration.
3. If placed inside a structural folder, make sure the folder contains a `.gradle-structure` file.
4. Your module will be automatically included during the next build or IDE sync.

---

## Running the Build

- Use the Gradle **wrapper** (`./gradlew`) for all builds.
- Run `./gradlew clean build` to verify correctness.
- If using IntelliJ, prefer "Open Gradle project" instead of importing modules manually.

---

## Resources

- [README.md](./README.md) – Project overview and purpose
- [CONVENTIONS.md](./CONVENTIONS.md) – Project layout and structural rules
