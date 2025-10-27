# GOSS Developer Setup Guide

This guide helps you set up a development environment for the GOSS (GridOPTICS Software System) platform using either Eclipse IDE or Visual Studio Code.

## Prerequisites

### Required Software

- **Java 22** (OpenJDK recommended)
- **Git** for version control
- **Gradle 8.10+** (included via Gradle wrapper)

### Installing Java 22

#### Using SDKMAN (Recommended)
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source ~/.sdkman/bin/sdkman-init.sh

# Install Java 22
sdk install java 22.0.2-tem
sdk use java 22.0.2-tem
```

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install openjdk-22-jdk
```

#### macOS (Homebrew)
```bash
brew install openjdk@22
# Add to your shell profile:
export PATH="/opt/homebrew/opt/openjdk@22/bin:$PATH"
```

#### Windows
1. Download OpenJDK 22 from [Eclipse Adoptium](https://adoptium.net/)
2. Install and set `JAVA_HOME` environment variable
3. Add `%JAVA_HOME%\bin` to your `PATH`

### Verify Installation
```bash
java -version  # Should show Java 22.x.x
./gradlew --version  # Should work without errors
```

## Project Overview

GOSS is a modern OSGi-based messaging framework with the following structure:

```
GOSS/
├── pnnl.goss.core/              # Core GOSS framework
│   ├── src/pnnl/goss/core/     # Core API and interfaces
│   ├── client/                  # Client implementations
│   ├── server/                  # Server implementations
│   └── security/                # Security realms and handlers
├── pnnl.goss.core.runner/       # Executable runners
├── pnnl.goss.core.itests/       # Integration tests
├── pnnl.goss.core.testutil/     # Testing utilities
└── cnf/                         # BND workspace configuration
```

### Key Technologies
- **OSGi Declarative Services** (modern dependency injection)
- **Apache ActiveMQ** (message broker)
- **Apache Shiro** (security framework)
- **BND Tools** (OSGi bundle management)
- **JDK 22** (modern Java features)

## Eclipse IDE Setup

### Step 1: Install Eclipse IDE

Download **Eclipse IDE for Enterprise Java and Web Developers** (2023-12 or later) from [eclipse.org](https://www.eclipse.org/downloads/).

### Step 2: Install Required Plugins

#### BND Tools Plugin (Essential for OSGi Development)
1. Go to **Help → Eclipse Marketplace**
2. Search for "BND Tools"
3. Install **Bnd OSGi Tools** by Neil Bartlett
4. Restart Eclipse

#### Buildship Gradle Plugin (Usually pre-installed)
1. Go to **Help → Eclipse Marketplace**
2. Search for "Buildship Gradle Integration"
3. Install if not already present

### Step 3: Import GOSS Project

1. **Clone the Repository**
   ```bash
   git clone <your-goss-repository-url>
   cd GOSS
   ```

2. **Import as Gradle Project**
   - File → Import → Gradle → Existing Gradle Project
   - Browse to your GOSS directory
   - Click **Next** and **Finish**
   - Eclipse will automatically download dependencies and configure the project

3. **Configure Java Build Path**
   - Right-click project → Properties → Java Build Path
   - Verify **Modulepath** shows Java 22
   - If not, remove old JRE and add Java 22 JRE

### Step 4: Eclipse Project Configuration

#### Enable OSGi Development Features
1. **Window → Perspective → Open Perspective → Other → Plug-in Development**
2. This enables OSGi bundle editors and tools

#### Configure BND Workspace
1. The `cnf/` directory contains BND workspace configuration
2. Eclipse should automatically recognize this as a BND workspace
3. You'll see `.bnd` files with syntax highlighting

#### Set Up Run Configurations
1. **Right-click on `pnnl.goss.core.runner`** → Run As → Java Application
2. Choose `GossSimpleRunner` as the main class
3. Set VM arguments if needed:
   ```
   -Djava.util.logging.config.file=conf/logging.properties
   ```

### Step 5: Development Workflow in Eclipse

#### Building the Project
- **Gradle → Refresh Gradle Project** (right-click on project)
- **Project → Build All** for incremental builds
- **Run → External Tools → External Tools Configurations** to set up Gradle tasks

#### Running Integration Tests
1. Navigate to `pnnl.goss.core.itests/src/`
2. Right-click test classes → Run As → JUnit Test
3. Or use Gradle: **Gradle Tasks → verification → check**

#### Debugging
1. Set breakpoints in your code
2. Right-click `GossSimpleRunner` → Debug As → Java Application
3. Use Eclipse's debugging perspective for step-through debugging

## Visual Studio Code Setup

### Step 1: Install VS Code Extensions

#### Essential Extensions
```bash
# Install VS Code first, then add these extensions:
code --install-extension vscjava.vscode-java-pack
code --install-extension vscjava.vscode-gradle
code --install-extension ms-vscode.vscode-json
code --install-extension redhat.vscode-yaml
```

#### Java Extension Pack includes:
- Language Support for Java by Red Hat
- Debugger for Java
- Test Runner for Java
- Maven for Java
- Project Manager for Java
- Visual Studio IntelliCode

### Step 2: Open GOSS Project

1. **Clone and Open**
   ```bash
   git clone <your-goss-repository-url>
   cd GOSS
   code .
   ```

2. **Configure Java**
   - Press `Ctrl+Shift+P` (Cmd+Shift+P on macOS)
   - Type: **Java: Configure Java Runtime**
   - Set Java 22 as the project JDK

### Step 3: VS Code Configuration

#### Workspace Settings (`.vscode/settings.json`)
```json
{
    "java.home": "/path/to/java-22",
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.gradle.buildServer.enabled": "on",
    "files.exclude": {
        "**/.gradle": true,
        "**/build": true,
        "**/bin": true
    },
    "java.compile.nullAnalysis.mode": "automatic"
}
```

#### Launch Configuration (`.vscode/launch.json`)
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch GOSS Simple Runner",
            "request": "launch",
            "mainClass": "pnnl.goss.core.runner.GossSimpleRunner",
            "projectName": "pnnl.goss.core.runner",
            "console": "integratedTerminal",
            "args": [],
            "vmArgs": "-Djava.util.logging.config.file=conf/logging.properties"
        }
    ]
}
```

#### Tasks Configuration (`.vscode/tasks.json`)
```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "Build GOSS",
            "type": "shell",
            "command": "./gradlew",
            "args": ["build", "-x", "check"],
            "group": "build",
            "presentation": {
                "echo": true,
                "reveal": "always"
            }
        },
        {
            "label": "Run Tests",
            "type": "shell",
            "command": "./gradlew",
            "args": ["check"],
            "group": "test",
            "presentation": {
                "echo": true,
                "reveal": "always"
            }
        },
        {
            "label": "Create Executable JARs",
            "type": "shell",
            "command": "./gradlew",
            "args": [":pnnl.goss.core.runner:createSimpleRunner"],
            "group": "build",
            "presentation": {
                "echo": true,
                "reveal": "always"
            }
        }
    ]
}
```

### Step 4: VS Code Development Workflow

#### Building and Running
1. **Open Command Palette**: `Ctrl+Shift+P` (Cmd+Shift+P)
2. **Tasks: Run Task** → Select "Build GOSS"
3. **Run → Start Debugging** (F5) to run with debugger

#### Debugging
1. Set breakpoints by clicking left margin of code lines
2. Press **F5** to start debugging
3. Use Debug Console for runtime inspection

#### Testing
1. **Command Palette** → **Java: Run Tests**
2. Or use **Tasks: Run Task** → "Run Tests"
3. View results in Test Explorer panel

## Common Development Tasks

### Creating a New Request Handler

1. **Create Handler Class**
   ```java
   @Component
   public class MyRequestHandler implements RequestHandler {
       
       @Override
       public Response handle(Request request) {
           // Handle your request type
           return new MyResponse();
       }
       
       @Override
       public Class<? extends Request> getHandledRequestType() {
           return MyRequest.class;
       }
   }
   ```

2. **Register with OSGi**
   - The `@Component` annotation automatically registers the service
   - No additional configuration needed with OSGi DS

### Adding Security Authorization

1. **Create Authorization Handler**
   ```java
   @Component  
   public class MyAuthorizationHandler implements AuthorizationHandler {
       
       @Override
       public boolean isAuthorized(Request request, String username) {
           // Your authorization logic
           return true;
       }
   }
   ```

### Working with the Message Broker

1. **Creating a Client**
   ```java
   ClientFactory clientFactory = // injected via OSGi
   Client client = clientFactory.create("tcp://localhost:61617", "username", "password");
   
   // Send request
   Response response = client.getResponse(new MyRequest());
   ```

## Troubleshooting

### Common Issues

#### Java Version Problems
```bash
# Check current Java version
java -version

# Set JAVA_HOME (Linux/macOS)
export JAVA_HOME=/path/to/java-22

# Set JAVA_HOME (Windows)
set JAVA_HOME=C:\path\to\java-22
```

#### Gradle Issues
```bash
# Clean build
./gradlew clean build

# Refresh dependencies
./gradlew --refresh-dependencies build
```

#### OSGi Bundle Issues
- Check `.bnd` files for correct package exports
- Verify OSGi annotations are present (`@Component`, `@Reference`)
- Look at `generated/` directories for built bundles

#### IDE Not Recognizing Java 22 Features
- Verify IDE is using Java 22 for compilation
- Check project compiler compliance level
- Refresh/reimport the project

### Getting Help

1. **Check Logs**: Look in `logs/` directory for error messages
2. **Enable Debug Logging**: Add `-Djava.util.logging.level=FINE` to VM args
3. **OSGi Console**: Use Felix Gogo shell commands when running OSGi version

## Next Steps

After setting up your development environment:

1. **Run the Integration Tests**: `./gradlew check`
2. **Start the Simple Runner**: Run `GossSimpleRunner` main class
3. **Explore the Core API**: Look at classes in `pnnl.goss.core` package
4. **Create Your First Handler**: Follow the handler creation examples above

For production deployment, see [PRODUCTION-DEPLOYMENT.md](PRODUCTION-DEPLOYMENT.md).