# GridOPTICS Software System (GOSS)

Current GOSS build status: ![GOSS build status](https://travis-ci.org/GridOPTICS/GOSS.svg?branch=master)

**⚠️ IMPORTANT: JDK 22 UPGRADE ⚠️**
This branch has been updated to require OpenJDK 22. See the JDK 22 Upgrade section below for installation and migration details.

### Pre-Requisite
 1. OpenJDK 22 (or compatible JDK 22 distribution)
 
### Installing GOSS
User can chose to run pre-build GOSS jars or build from source code.

#### Running pre-build GOSS

 1. Clone the repository: `git clone https://github.com/GridOPTICS/GOSS-Release.git`
 1. Open terminal to the root of the cloned repository: `cd GOSS-Release`
 1. Execute `java -jar goss-core.jar`

#### Building from source code

 1. Clone the repository: `git clone https://github.com/GridOPTICS/GOSS.git`
 1. Open terminal to the root of the cloned repository
 1. Execute `./gradlew check` to run integration tests (optional but recommended)
 1. Execute `./gradlew :pnnl.goss.core.runner:createSimpleRunner` to build executable JAR
 1. Change to the executable directory: `cd pnnl.goss.core.runner/build/executable`
 1. Execute `java -jar goss-simple-runner.jar`

For SSL-enabled secure deployment:
 1. Execute `./gradlew :pnnl.goss.core.runner:createSSLRunner`
 1. Change to the executable directory: `cd pnnl.goss.core.runner/build/executable`
 1. Execute `java -jar goss-ssl-runner.jar`
 
The framework should be started now.  Default commands that goss uses are:

    gs:listDataSources   - Lists the known datasources that have been registered with the server.
    gs:listHandlers      - Lists the known request handlers that have been registered with the server. 
   
## Documentation

- **[Quick Start Guide](QUICK-START.md)** - Get up and running with GOSS in 5 minutes
- **[Developer Setup](DEVELOPER-SETUP.md)** - Complete development environment setup for Eclipse and VS Code
- **[Production Deployment](PRODUCTION-DEPLOYMENT.md)** - Production deployment guide with systemd, SSL, and monitoring

For additional information, see the [wiki](https://github.com/GridOPTICS/GOSS/wiki).

## JDK 22 Upgrade

### Installing OpenJDK 22

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-22-jdk
export JAVA_HOME=/usr/lib/jvm/java-22-openjdk-amd64
```

**CentOS/RHEL/Fedora:**
```bash
sudo dnf install java-22-openjdk-devel  # Fedora
sudo yum install java-22-openjdk-devel  # CentOS/RHEL
```

**macOS (Homebrew):**
```bash
brew install openjdk@22
export PATH="/usr/local/opt/openjdk@22/bin:$PATH"
```

**Windows:**
Download from [Adoptium](https://adoptium.net/) or [OpenJDK](https://jdk.java.net/22/)

**Using SDKMAN (recommended for development):**
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 22.0.2-tem
sdk use java 22.0.2-tem
```

### Major Changes in JDK 22 Version

1. **Updated Dependencies**: All major dependencies updated for JDK 22 compatibility
   - Spring Framework 6.x
   - Apache Shiro 1.13.x  
   - Jackson 2.17.x
   - SLF4J 2.x

2. **Build System**: Updated to Gradle 8.10 with modern BND tooling

3. **OSGi**: Updated to OSGi R7+ specifications

4. **Removed APIs**: Code updated to replace APIs removed after Java 8

### Migration Notes

- Ensure `JAVA_HOME` points to JDK 22
- Some configuration files may need updates for new dependency versions
- Review custom security configurations as Shiro APIs have changed
- Test thoroughly as many transitive dependencies have been updated
