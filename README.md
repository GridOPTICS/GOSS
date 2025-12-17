# GridOPTICS Software System (GOSS)

[![Build Status](https://github.com/GridOPTICS/GOSS/actions/workflows/build.yml/badge.svg)](https://github.com/GridOPTICS/GOSS/actions)

GOSS is a JMS-based messaging framework providing client/server architecture, request/response patterns, and security integration for distributed power grid applications. It serves as the foundation for [GridAPPS-D](https://github.com/GRIDAPPSD/GOSS-GridAPPS-D) and other grid simulation platforms.

**⚠️ IMPORTANT: Java 21 + Jakarta EE Migration ⚠️**
This project has been upgraded to **Java 21** with modern dependencies:
- **ActiveMQ 6.2.0** with **Jakarta JMS 3.1** (from javax.jms)
- **Apache Shiro 2.0.0** (from 1.x)
- **Jakarta EE APIs** (from Java EE)
- **Spring Framework 6.x**, **Jackson 2.18.x**, **SLF4J 2.0.x**

See the [JDK 21 Upgrade](#jdk-21-upgrade) section below for details.

## Quick Start

### Prerequisites
- **OpenJDK 21** (or compatible JDK 21 distribution)
- **Gradle 8.10+** (included via wrapper)

### Building and Running GOSS

**1. Clone the repository:**
```bash
git clone https://github.com/GridOPTICS/GOSS.git
cd GOSS
```

**2. Build and run (choose one):**

#### Option A: Simple Runner (Non-OSGi, Single JAR)
```bash
./gradlew :pnnl.goss.core.runner:createSimpleRunner
java -jar pnnl.goss.core.runner/generated/executable/goss-simple-runner.jar
```
- **Size**: ~33 MB
- **Type**: Fat JAR with all dependencies
- **Use case**: Quick testing, development

#### Option B: OSGi Runner (Modular, Production-Ready)
```bash
./gradlew buildRunner.goss-core
java -jar pnnl.goss.core.runner/generated/runners/goss-core-runner.jar
```
- **Size**: ~62 MB
- **Type**: Apache Felix OSGi framework with bundles
- **Use case**: Production, modular deployments
- **Includes**: Updated dependencies (ActiveMQ 6.2.0, Jakarta JMS, Shiro 2.0)

#### Option C: SSL-Enabled OSGi Runner
```bash
./gradlew buildRunner.goss-core-ssl
java -jar pnnl.goss.core.runner/generated/runners/goss-core-ssl-runner.jar
```

**3. Verify GOSS is running:**

Once started, you can use these Gogo shell commands:
```
gs:listDataSources   - Lists registered datasources
gs:listHandlers      - Lists registered request handlers
```

## Building Custom OSGi Runners

GOSS includes a **BndRunnerPlugin** that creates executable OSGi JARs from any `.bndrun` file.

### Using the Plugin

**For any `.bndrun` file in your project:**
```bash
./gradlew buildRunner.<name>
```

**Examples:**
```bash
./gradlew buildRunner.goss-core      # Uses goss-core.bndrun
./gradlew buildRunner.goss-core-ssl  # Uses goss-core-ssl.bndrun
./gradlew buildRunner.my-app         # Uses my-app.bndrun
```

### Creating Your Own .bndrun File

Create a file like `my-app.bndrun`:
```properties
# OSGi Framework
-runfw: org.apache.felix.framework;version='[7.0.5,8)'
-runee: JavaSE-21

# Bundles to include
-runbundles: \
    ${activemq-runpath},\
    ${jakarta-runpath},\
    ${slf4j-runpath},\
    pnnl.goss.core.core-api;version=latest,\
    pnnl.goss.core.goss-client;version=latest,\
    pnnl.goss.core.goss-core-server;version=latest

# Runtime properties
-runproperties: \
    activemq.host=0.0.0.0,\
    openwire.port=61616,\
    stomp.port=61613
```

Then build it:
```bash
./gradlew buildRunner.my-app
java -jar pnnl.goss.core.runner/generated/runners/my-app-runner.jar
```

### Applying BndRunnerPlugin to Your Own Project

The plugin is available in `buildSrc/` and can be used in other projects:

**1. Copy buildSrc to your project:**
```bash
cp -r GOSS/buildSrc /path/to/your/project/
```

**2. Apply the plugin in your `build.gradle`:**
```gradle
apply plugin: com.pnnl.goss.gradle.BndRunnerPlugin

bndRunner {
    bundleDirs = [
        file('generated'),
        file('../GOSS/pnnl.goss.core/generated')  // Include GOSS bundles
    ]
    configDir = file('conf')
}
```

**3. Use it:**
```bash
cd /path/to/your/project
./gradlew buildRunner.my-runtime
``` 
   
## Version 11.0.0 Features

### JWT Token Authentication Support
GOSS now includes optional JWT (JSON Web Token) authentication support:

```java
// Create client with token authentication
ClientFactory factory = // ... get factory
Client client = factory.create(PROTOCOL.OPENWIRE, credentials, true); // useToken=true
```

**New Security Classes:**
- `JWTAuthenticationToken` - Token data structure and parsing
- `SecurityConfig` - Token validation and creation interface
- `GossSecurityManager` - Enhanced security management
- `RoleManager` - Role-based permission management

**Security Configuration:**
```properties
goss.system.use.token=true
goss.system.token.secret=your-secret-key
goss.system.manager=admin
goss.system.manager.password=admin-password
```

### Session Auto-Renewal
Clients now automatically renew their JMS session when publish operations fail, improving reliability in long-running applications.

### Version Management

GOSS includes a Makefile for common build and release tasks:

```bash
# Show versions of all bundles
make version

# Set release version (removes -SNAPSHOT)
make release VERSION=11.0.0

# Set snapshot version (adds -SNAPSHOT)
make snapshot VERSION=11.1.0

# Build all bundles
make build

# Run tests
make test
```

### Publishing to GOSS-Repository

GOSS bundles can be published to a local [GOSS-Repository](https://github.com/GridOPTICS/GOSS-Repository) clone for OSGi resolution:

```bash
# Push snapshot JARs to ../GOSS-Repository/snapshot/
make push-snapshot

# Push release JARs to ../GOSS-Repository/release/
make push-release
```

**Note:** The GOSS-Repository must be cloned as a sibling directory (`../GOSS-Repository`). This is the local repository used for BND workspace resolution, not a remote Maven repository.

## Documentation

### Getting Started
- **[Quick Start Guide](docs/QUICK-START.md)** - Get up and running with GOSS in 5 minutes
- **[Developer Setup](docs/DEVELOPER-SETUP.md)** - Complete development environment setup for Eclipse and VS Code
- **[Production Deployment](docs/PRODUCTION-DEPLOYMENT.md)** - Production deployment guide with systemd, SSL, and monitoring

### Development
- **[Code Formatting Guide](docs/FORMATTING.md)** - Code style and formatting configuration for consistent code across IDEs

### Additional Resources
- [Documentation Index](docs/README.md) - Complete documentation hub
- [Issue Tracker](https://github.com/GridOPTICS/GOSS/issues) - Report bugs or request features

## JDK 21 + Jakarta EE Migration

### Installing OpenJDK 21

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

**CentOS/RHEL/Fedora:**
```bash
sudo dnf install java-21-openjdk-devel  # Fedora
sudo yum install java-21-openjdk-devel  # CentOS/RHEL
```

**macOS (Homebrew):**
```bash
brew install openjdk@21
export PATH="/usr/local/opt/openjdk@21/bin:$PATH"
```

**Windows:**
Download from [Adoptium](https://adoptium.net/) or [OpenJDK](https://jdk.java.net/21/)

**Using SDKMAN (recommended for development):**
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.5-tem
sdk use java 21.0.5-tem
```

### Major Changes in This Version

#### 1. Jakarta EE Migration (javax → jakarta)
**Before (Java EE):**
```java
import javax.jms.Connection;
import javax.annotation.PostConstruct;
```

**After (Jakarta EE):**
```java
import jakarta.jms.Connection;
import jakarta.annotation.PostConstruct;
```

**Updated packages:**
- `jakarta.jms:jakarta.jms-api:3.1.0` (was `javax.jms`)
- `jakarta.annotation:jakarta.annotation-api:2.1.1`
- `jakarta.resource:jakarta.resource-api:2.1.0`
- `jakarta.transaction:jakarta.transaction-api:2.0.1`
- `jakarta.inject:jakarta.inject-api:2.0.1`
- `jakarta.xml.bind:jakarta.xml.bind-api:4.0.2`

#### 2. ActiveMQ 6.x with Jakarta JMS
- **ActiveMQ 6.2.0** (was 5.15.x)
- Native Jakarta JMS support
- No more shim/bridge layers
- Updated broker configuration

#### 3. Apache Shiro 2.0
- **Shiro 2.0.0** (was 1.x)
- API changes in authentication/authorization
- Updated security configuration

#### 4. Modern Dependencies
- **Spring Framework 6.2.0** (was 5.x)
- **Jackson 2.18.1** (was 2.17.x)
- **SLF4J 2.0.16** (was 1.7.x)
- **Apache Felix 7.0.5** OSGi framework
- **Gradle 8.10** with BND 6.4.0

#### 5. OSGi Improvements
- Updated to OSGi R8 specifications
- Modular BndRunnerPlugin for creating custom OSGi runners
- Improved bundle dependency resolution

### Migration Checklist

✅ **Environment:**
- [ ] Install JDK 21
- [ ] Set `JAVA_HOME` to JDK 21
- [ ] Verify: `java -version` shows 21.x

✅ **Code Updates (if extending GOSS):**
- [ ] Replace `javax.jms.*` with `jakarta.jms.*`
- [ ] Replace other `javax.*` EE packages with `jakarta.*`
- [ ] Update Shiro security configurations for 2.0 API changes
- [ ] Test ActiveMQ connections (configuration may need updates)

✅ **Build:**
- [ ] Update Gradle wrapper if using older version
- [ ] Clear Gradle cache: `./gradlew clean`
- [ ] Run tests: `./gradlew check`

### Breaking Changes

**ActiveMQ Configuration:**
- Old broker URLs still work, but new features require updated configuration
- SSL/TLS configuration has changed in ActiveMQ 6.x

**Shiro Security:**
- Some authentication realm APIs have changed
- Review custom `Realm` implementations

**Removed Java EE APIs:**
- All `javax.jms`, `javax.annotation`, etc. → Use Jakarta equivalents

## Project Structure

```
GOSS/
├── pnnl.goss.core/                # Core bundles
│   ├── core-api/                  # Core API interfaces
│   ├── goss-client/               # Client implementation
│   ├── goss-core-server/          # Server implementation
│   ├── goss-core-server-api/      # Server API interfaces
│   ├── goss-core-security/        # Security integration (Shiro)
│   └── security-propertyfile/     # Property-file authentication
├── pnnl.goss.core.runner/         # Executable runners
│   ├── goss-core.bndrun           # OSGi runtime definition
│   └── conf/                      # Runtime configuration
├── buildSrc/                      # Gradle plugins (BndRunnerPlugin)
├── cnf/                           # BND workspace configuration
├── scripts/                       # Build and release scripts
├── Makefile                       # Build automation
└── push-to-local-goss-repository.py  # Repository publishing tool
```

## Users of GOSS

GOSS serves as the messaging foundation for:

- **[GridAPPS-D](https://github.com/GRIDAPPSD/GOSS-GridAPPS-D)** - Grid Application Platform for Planning and Simulation with Distribution. GridAPPS-D is built as an OSGi application on top of GOSS, using its messaging framework for simulation orchestration, data management, and application integration.

## Technology Stack

- **Java 21** with modern language features
- **OSGi** (Apache Felix 7.0.5) for modular service architecture
- **BND Tools 6.4.0** for OSGi bundle management
- **Apache ActiveMQ 6.2.0** for JMS messaging (Jakarta EE compatible)
- **Apache Shiro 2.0** for authentication and authorization
- **Gradle 8.10** build system

## Related Repositories

| Repository | Description |
|------------|-------------|
| [GOSS-GridAPPS-D](https://github.com/GRIDAPPSD/GOSS-GridAPPS-D) | GridAPPS-D platform built on GOSS |
| [GOSS-Repository](https://github.com/GridOPTICS/GOSS-Repository) | OSGi bundle repository for BND resolution |
| [gridappsd-docker](https://github.com/GRIDAPPSD/gridappsd-docker) | Docker deployment for GridAPPS-D |
| [gridappsd-python](https://github.com/GRIDAPPSD/gridappsd-python) | Python client library |

## License

This project is licensed under the BSD-3-Clause License. See [LICENSE](LICENSE) for details.

## Contributing

Contributions are welcome! Please submit pull requests to the `develop` branch.

## Support

- **Documentation**: See the [docs/](docs/) directory
- **Issues**: https://github.com/GridOPTICS/GOSS/issues
