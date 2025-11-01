# GOSS Documentation

Complete documentation for the GridOPTICS Software System (GOSS).

## Getting Started

### [Quick Start Guide](QUICK-START.md)

Get up and running with GOSS in 5 minutes. Covers installation, building, and running your first GOSS server.

**Topics:**

- Building GOSS from source
- Running the GOSS server
- Testing with example clients
- Common troubleshooting

### [Developer Setup](DEVELOPER-SETUP.md)

Complete development environment setup for both Eclipse and VS Code IDEs.

**Topics:**

- IDE configuration (Eclipse & VS Code)
- Java 21 setup with SDKMAN
- Gradle and BND build system
- Creating custom handlers
- Debugging GOSS applications
- OSGi bundle development

## Development Guides

### [Code Formatting Guide](FORMATTING.md)

Code style and formatting configuration for consistent code across IDEs.

**Topics:**

- Eclipse and VS Code formatter setup
- Spotless Gradle plugin usage
- Pre-commit hooks
- CI/CD formatting checks
- Troubleshooting formatter issues

## Deployment

### [Production Deployment Guide](PRODUCTION-DEPLOYMENT.md)

Production deployment guide with systemd, SSL, and monitoring.

**Topics:**

- Systemd service configuration
- SSL/TLS setup
- Production best practices
- Monitoring and logging
- Performance tuning
- Security hardening

## Architecture Overview

### Core Components

**pnnl.goss.core** - Main module containing:

- Client/Server APIs
- Request/Response framework
- Security implementations (Shiro-based)
- Web services (JAX-RS REST endpoints)

**pnnl.goss.core.runner** - Executable runner:

- Example handlers and configurations
- Pre-configured runners (simple, SSL, full)
- Standalone JAR generation

**pnnl.goss.core.itests** - Integration tests:

- Full stack testing
- OSGi bundle testing
- End-to-end scenarios

**pnnl.goss.core.testutil** - Test utilities:

- Shared test infrastructure
- Mock implementations
- Test helpers

### Technology Stack

- **Build**: Gradle 8.10 + BND 6.4.0
- **Runtime**: Java 21 (OpenJDK/Temurin)
- **Messaging**: Apache ActiveMQ 5.18.6
- **OSGi**: R7 specifications
- **Security**: Apache Shiro 1.13.x
- **Web**: JAX-RS with Jersey
- **Logging**: SLF4J 2.x

## Quick Reference

### Build Commands

```bash
# Build everything
./gradlew build

# Build without integration tests
./gradlew build -x check

# Run integration tests only
./gradlew check

# Create executable JARs
./gradlew export

# Check code formatting
./gradlew spotlessCheck

# Fix code formatting
./gradlew spotlessApply
```

### Running GOSS

```bash
# Navigate to runner directory
cd pnnl.goss.core.runner/generated/executable

# Run simple runner (no authentication)
java -jar goss-simple-runner.jar

# Run with SSL
java -jar goss-ssl-runner.jar

# Run full GOSS with all features
java -jar goss-core-runner.jar
```

### GOSS Shell Commands

Once GOSS is running, use these commands:

- `gs:listDataSources` - List registered datasources
- `gs:listHandlers` - List registered request handlers

## Contributing

### Code Style

- Follow Eclipse formatter configuration (`.settings/eclipse-java-formatter.xml`)
- Run `./gradlew spotlessApply` before committing
- See [FORMATTING.md](FORMATTING.md) for details

### Pull Requests

1. Create a feature branch from `master`
2. Make your changes
3. Run `./gradlew build` to ensure it compiles
4. Run `./gradlew spotlessApply` to format code
5. Submit PR with clear description

### Testing

- Write unit tests for new functionality
- Ensure integration tests pass: `./gradlew check`
- Test in both development and production modes

## Support

- **Issues**: [GitHub Issues](https://github.com/GridOPTICS/GOSS/issues)
- **Discussions**: Use GitHub Discussions for questions
- **Documentation**: All documentation is in this repository under `/docs`

## License

See [LICENSE](../LICENSE) file in the root directory.
