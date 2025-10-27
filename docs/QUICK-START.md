# GOSS Quick Start Guide

Get up and running with GOSS in 5 minutes.

## Prerequisites

- **Java 22** installed
- **Git** for cloning the repository

## 1. Clone and Build

```bash
git clone <your-repository-url>
cd GOSS

# Verify Java 22
java -version

# Build executable JARs
./gradlew :pnnl.goss.core.runner:createSimpleRunner
```

## 2. Run GOSS

```bash
# Navigate to executable
cd pnnl.goss.core.runner/generated/executable

# Start GOSS (will run until Ctrl+C)
java -jar goss-simple-runner.jar
```

You should see:
```
Starting GOSS Simple Runner...
GOSS Core services are running
ActiveMQ Broker: tcp://0.0.0.0:61617
STOMP: tcp://0.0.0.0:61618
GOSS Simple Runner started successfully!
Press Ctrl+C to stop
```

## 3. Test Connection

### Using Java Client
```java
// Connect to GOSS
ClientFactory factory = new ClientFactoryImpl();
Client client = factory.create("tcp://localhost:61617");

// Send a message
MyRequest request = new MyRequest();
Response response = client.getResponse(request);
```

### Using Command Line (STOMP)
```bash
# Install STOMP client (optional)
npm install -g stomp-client

# Connect and send message
stomp connect stomp://localhost:61618
stomp send /queue/test "Hello GOSS!"
```

## 4. What's Running?

GOSS provides:
- **Message Broker**: ActiveMQ on port 61617 (OpenWire) and 61618 (STOMP)
- **Request/Response**: Synchronous and asynchronous messaging
- **Security Framework**: Apache Shiro (currently disabled for simplicity)
- **Extensible Handlers**: Plugin architecture for custom request processing

## Next Steps

### For Developers
- Read [DEVELOPER-SETUP.md](DEVELOPER-SETUP.md) for IDE setup
- Explore `pnnl.goss.core/src/` for API documentation
- Run integration tests: `./gradlew check`

### For Production
- Read [PRODUCTION-DEPLOYMENT.md](PRODUCTION-DEPLOYMENT.md) for deployment guide
- Configure SSL/TLS for security
- Set up monitoring and logging

### Create Your First Handler
```java
@Component
public class HelloWorldHandler implements RequestHandler {
    
    @Override
    public Response handle(Request request) {
        return new HelloWorldResponse("Hello from GOSS!");
    }
    
    @Override
    public Class<? extends Request> getHandledRequestType() {
        return HelloWorldRequest.class;
    }
}
```

## Troubleshooting

**Port already in use?**
```bash
# Check what's using port 61617
sudo netstat -tlnp | grep 61617

# Or modify the ports in GossSimpleRunner.java and rebuild
```

**Java version issues?**
```bash
# Make sure you're using Java 22
export JAVA_HOME=/path/to/java-22
java -version
```

**Build failures?**
```bash
# Clean build
./gradlew clean build
```

## Architecture Overview

```
┌─────────────────────────────────────────────┐
│                 GOSS Platform               │
├─────────────────────────────────────────────┤
│  Request Handlers  │  Security Framework   │
│  ┌───────────────┐  │  ┌─────────────────┐  │
│  │ Custom        │  │  │ Apache Shiro    │  │
│  │ Handlers      │  │  │ Authentication  │  │
│  └───────────────┘  │  │ Authorization   │  │
│                     │  └─────────────────┘  │
├─────────────────────────────────────────────┤
│            Core GOSS Framework              │
│  ┌─────────────────────────────────────────┐ │
│  │          Request/Response API           │ │
│  │    Client Factory │ Message Routing    │ │
│  └─────────────────────────────────────────┘ │
├─────────────────────────────────────────────┤
│           Apache ActiveMQ Broker           │
│  ┌───────────┐ ┌──────────┐ ┌─────────────┐ │
│  │ OpenWire  │ │  STOMP   │ │ Persistence │ │
│  │:61617     │ │  :61618  │ │   KahaDB    │ │
│  └───────────┘ └──────────┘ └─────────────┘ │
└─────────────────────────────────────────────┘
```

**Congratulations!** You now have GOSS running. Start building your distributed messaging applications!