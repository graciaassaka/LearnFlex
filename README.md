## LearnFlex Project Setup Guide

Welcome to the LearnFlex project! This guide will walk you through the initial project setup, ensuring you have all
necessary tools and configurations in place to start development.

### 1. **Project Structure and Configuration**

The project structure follows standard Kotlin Multiplatform guidelines to make navigation easy and ensure consistency
across the team. To get started:

- Clone the GitHub repository using: `git clone <repo_url>`.
- Folder structure and key files include:
    - **AndroidManifest**: Located under the Android-specific folders, defines permissions and app configurations.
    - **commonMain**: The shared code for both desktop and Android versions.
    - IntelliJ project files and Qodana configuration are included for enhanced code quality checks.

Refactoring for consistency has been done across package imports and file paths.

### 2. **GitHub Repository Setup**

The LearnFlex repository is hosted on GitHub for version control:

- We have set up branch strategies to manage stable and development versions.
- A `.gitignore` file has been added to exclude unnecessary files from version control.
- The repository includes a basic README file for project documentation.

### 3. **IDE Setup and Project Initialization**

We recommend using **IntelliJ IDEA** as the IDE:

- Install IntelliJ IDEA and open the LearnFlex Kotlin Multiplatform project.
- Configure GitHub integration within IntelliJ to commit and push changes directly.

### 4. **Gradle Build Tool Configuration**

Gradle is the build tool for LearnFlex, managing dependencies and build tasks for Kotlin Multiplatform:

- Set up project dependencies and plugins for Kotlin, Android, and JVM.
- Updates include Android Gradle Plugin (AGP) version and library versions for compatibility.
- Ensure the build runs smoothly for Android and desktop targets.

### 5. **Dependency Management with Koin**

Dependency injection is managed using **Koin**:

- Added Koin dependencies in `libraries.versions.toml`.
- Set up initial Koin modules to handle project dependencies.
- Initial test classes were created to ensure the correct functionality of Koin modules.

### 6. **Platform Target Setup**

LearnFlex targets both Android and desktop platforms:

- Configurations include `minSdkVersion`, `targetSdkVersion` for Android.
- Make sure both platforms can successfully build by running `./gradlew build`.

### 7. **Firebase Integration**

Firebase is used for backend functionality, including real-time database and analytics:

- Set up a Firebase project and integrated Android and desktop versions.
- Added `google-services.json` for Android and corresponding Firebase dependencies to `build.gradle.kts`.

### 8. **Environment Configuration Management**

Sensitive data, such as API keys, is managed through an environment file:

- Create an `.env` file to store sensitive information.
- Add the `.env` file to `.gitignore` to prevent it from being tracked by version control.
- Use an appropriate library to read environment variables within Kotlin.

### 9. **Testing and Documentation**

- Initial unit tests have been written for Koin modules and Firebase integration.
- Documenting the setup process ensures all developers have the same foundation and understanding of the project
  structure.

For further details on development practices or troubleshooting, please refer to the documentation provided within the
repository or contact the project maintainer.

