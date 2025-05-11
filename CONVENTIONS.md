# Gradle Build & Module Conventions

This document defines the structural and naming conventions used within this project’s Gradle build. It helps ensure a consistent, maintainable layout and avoids common issues during development, CI builds, and IDE usage.

---

## Project Structure

This project is organized as a **multi-module Gradle build**.  
The build is defined in `settings.gradle`, and includes individual modules based on folder structure and presence of `build.gradle` files.

### Root-Level Modules

The most important modules live as direct subdirectories of the root project.

Each of these contains its own `build.gradle` and is included as a Gradle project with a flat path (e.g., `:app`, `:core`).

### Structural Folders

To keep the root directory organized, related modules can be grouped under **structural folders**.  
These folders themselves are **not treated as Gradle modules**, but may contain multiple valid submodules.

To mark such a folder explicitly, create a `.gradle-structure` file inside it.

#### Behavior

- The folder **itself is excluded** from Gradle's `include(...)`
- **Direct subfolders** are evaluated and included **if they contain a `build.gradle` file**
- Nested subfolders (depth > 1) are **not scanned**

---

## 📎 See Also

- [CONTRIBUTING.md](./CONTRIBUTING.md) – for contribution workflows and setup
- [README.md](./README.md) – for general project overview
