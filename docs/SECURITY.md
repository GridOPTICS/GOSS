# GOSS Security Guide

GOSS uses [Apache Shiro](https://shiro.apache.org/) for authentication and authorization, integrated with ActiveMQ via the [ShiroPlugin](https://activemq.apache.org/security). All broker connections (OpenWire, STOMP) require valid credentials and are subject to permission checks.

## User Management

### Property File Configuration

Users are defined in a property file with one user per line:

**File**: `pnnl.goss.core.runner/conf/pnnl.goss.core.security.propertyfile.cfg`

**Format**:
```
username=password,permission1,permission2,...
```

**Example**:
```properties
system=manager,queue:*,topic:*,temp-queue:*
craig=craig,queue:*,topic:*,temp-queue:*
july=july,queue:*,topic:*,temp-queue:*
```

Each line defines:
- **username** - The login name (left of `=`)
- **password** - The first value after `=` (plaintext)
- **permissions** - Comma-separated permission strings

### Adding a New User

Add a line to the property file and restart GOSS:

```properties
alice=secretpass,queue:*,topic:*,temp-queue:*
```

For a read-only user that can only subscribe to topics:

```properties
reader=readerpass,topic:*
```

### System User

The `system` user is the administrative account used internally by GOSS for broker management and token handling. It must always be present with at least `queue:*,topic:*,temp-queue:*` permissions.

## Permissions

### Permission Format

Permissions use a colon-separated hierarchical format:

```
type:destination[:action]
```

| Part | Description | Examples |
|------|-------------|----------|
| **type** | Destination type | `queue`, `topic`, `temp-queue` |
| **destination** | Destination name or wildcard | `*`, `goss.gridappsd.process.request`, `ActiveMQ.Advisory.*` |
| **action** | Optional action | `read`, `write`, `create`, `subscribe` |

### Wildcards

The `*` wildcard matches any value at that level:

| Permission | Grants access to |
|------------|-----------------|
| `queue:*` | All queues, all actions |
| `topic:*` | All topics, all actions |
| `temp-queue:*` | All temporary queues |
| `topic:goss.gridappsd.*` | All topics under `goss.gridappsd` |
| `queue:request:write` | Write-only access to the `request` queue |

### Common Permission Sets

**Full access** (admin/system users):
```
queue:*,topic:*,temp-queue:*
```

**GridAPPS-D application** (typical simulation app):
```
topic:goss.gridappsd.simulation.*,queue:goss.gridappsd.process.request,temp-queue:*
```

**Read-only subscriber**:
```
topic:*
```

### How Permissions Are Enforced

GOSS uses `GossWildcardPermissionResolver` to route permission checks:

- Permissions starting with `topic:`, `queue:`, or `temp-queue:` are handled by ActiveMQ's `ActiveMQWildcardPermission` and enforced at the broker level
- All other permissions use Shiro's standard `WildcardPermission` for application-level access control

When a client attempts to send to or subscribe on a destination, ActiveMQ's ShiroPlugin checks the client's permissions against the requested destination. If the permission check fails, the operation is rejected.

## JWT Token Authentication

JWT (JSON Web Token) authentication allows clients to authenticate once with credentials and then reconnect using a token, avoiding repeated credential transmission.

### Token Flow

```
Client                                    GOSS Server
  |                                            |
  |  1. CONNECT (username/password)            |
  |-------------------------------------------->|
  |                                            |
  |  2. CONNECTED                              |
  |<--------------------------------------------|
  |                                            |
  |  3. SUBSCRIBE /queue/temp.reply.xyz        |
  |-------------------------------------------->|
  |                                            |
  |  4. SEND to /topic/pnnl.goss.token.topic   |
  |     body: base64(username:password)         |
  |     reply-to: /queue/temp.reply.xyz         |
  |-------------------------------------------->|
  |                                            |
  |  5. MESSAGE on /queue/temp.reply.xyz       |
  |     body: <JWT token>                       |
  |<--------------------------------------------|
  |                                            |
  |  6. DISCONNECT                             |
  |-------------------------------------------->|
  |                                            |
  |  7. CONNECT (token as username, empty pass) |
  |-------------------------------------------->|
  |                                            |
  |  8. CONNECTED (authenticated via token)    |
  |<--------------------------------------------|
```

### Step-by-step

1. **Connect with credentials** - Standard STOMP/OpenWire connection with username and password
2. **Subscribe to a reply queue** - Create a temporary queue to receive the token response
3. **Request a token** - Send `base64(username:password)` to the token topic (`/topic/pnnl.goss.token.topic`) with a `reply-to` header pointing to your reply queue
4. **Receive the token** - The server validates credentials and responds with a JWT token (or `"authentication failed"`)
5. **Reconnect with token** - Disconnect and reconnect using the JWT token as the username with an empty password

### Token Structure

Tokens are signed with HMAC-SHA256 (HS256) and contain:

| Claim | Description |
|-------|-------------|
| `sub` | Username |
| `roles` | List of permission strings |
| `iat` | Issued-at timestamp |
| `exp` | Expiration (5 days from issuance) |

### STOMP Client Example (Python)

```python
import base64
import stomp
import time

HOST = "localhost"
PORT = 61618
USERNAME = "system"
PASSWORD = "manager"
TOKEN_TOPIC = "/topic/pnnl.goss.token.topic"

# Step 1: Connect with credentials
conn = stomp.Connection12([(HOST, PORT)])
conn.connect(USERNAME, PASSWORD, wait=True)

# Step 2: Set up a listener for the token response
class TokenListener(stomp.ConnectionListener):
    def __init__(self):
        self.token = None
    def on_message(self, frame):
        self.token = frame.body

listener = TokenListener()
conn.set_listener("token", listener)

# Step 3: Subscribe to reply queue and request token
reply_queue = "/queue/temp.token.reply"
conn.subscribe(destination=reply_queue, id="1", ack="auto")
time.sleep(0.3)

payload = base64.b64encode(f"{USERNAME}:{PASSWORD}".encode()).decode()
conn.send(
    destination=TOKEN_TOPIC,
    body=payload,
    headers={"reply-to": reply_queue},
)

# Step 4: Wait for token
time.sleep(2)
token = listener.token
print(f"Got token: {token[:50]}...")
conn.disconnect()

# Step 5: Reconnect with token (token as username, empty password)
conn2 = stomp.Connection12([(HOST, PORT)])
conn2.connect(token, "", wait=True)
print("Connected with token!")
conn2.disconnect()
```

### Java Client Example

```java
// Using GOSS ClientFactory with token support
ClientFactory factory = // ... get from OSGi or direct instantiation
Credentials creds = new UsernamePasswordCredentials(username, password);
Client client = factory.create(PROTOCOL.OPENWIRE, creds, true); // useToken=true
```

### Security Configuration (OSGi)

For OSGi deployments, JWT settings are configured via:

```properties
goss.system.manager=system
goss.system.manager.password=manager
```

The JWT signing key is derived automatically from the system manager credentials. For custom key management, implement the `SecurityConfig` interface.

## Architecture

### Authentication Realms

GOSS registers multiple Shiro realms with the security manager:

| Realm | Purpose | Authentication method |
|-------|---------|----------------------|
| **PropertyRealm** | Property-file users | Username + password |
| **TokenRealm** | JWT token users | Token as username, empty password |
| **SystemRealm** (OSGi) | System admin account | Hardcoded system credentials |

Token-based authentication is detected by: username length > 250 characters and empty password. This heuristic works because JWT tokens are always longer than any reasonable username.

### Key Security Classes

| Class | Package | Role |
|-------|---------|------|
| `SecurityConfigImpl` | `pnnl.goss.core.security.impl` | JWT token creation and validation (HS256) |
| `JWTAuthenticationToken` | `pnnl.goss.core.security` | JWT claims data structure |
| `GossWildcardPermissionResolver` | `pnnl.goss.core.security.impl` | Routes ActiveMQ vs. application permissions |
| `PropertyBasedRealm` | `pnnl.goss.core.security.propertyfile` | Property-file user authentication (OSGi) |
| `GossSecurityManager` | `pnnl.goss.core.security` | Central security coordination |
| `RoleManager` | `pnnl.goss.core.security` | Role-to-permission mapping |

### SimpleRunner vs. OSGi Security

The **SimpleRunner** (`GossSimpleRunner`) wires security directly without OSGi:
- Reads users from the property file at startup
- Creates `PropertyRealm` and `TokenRealm` as inner classes
- Handles token requests via a direct JMS listener on the token topic
- Attaches `ShiroPlugin` to the embedded ActiveMQ broker

The **OSGi Runner** uses Declarative Services to wire security components:
- `PropertyBasedRealm`, `SystemRealm`, `UnauthTokenBasedRealm` are OSGi components
- `UserRepositoryImpl` handles token requests via GOSS Client abstractions
- `RoleManagerImpl` provides role-to-permission mapping from configuration files

## Integration Testing

Run the STOMP token authentication integration tests:

```bash
make itest
```

This builds GOSS, starts it in the background, runs the Python test suite against the STOMP port, and stops GOSS when done. The tests verify:

1. Credential-based STOMP connection
2. JWT token request and response
3. Token-based reconnection
4. Publish/subscribe with token authentication
5. Invalid credential rejection
6. Empty token rejection

The test suite is in `pnnl.goss.core.itests/` and uses [pixi](https://pixi.sh/) for Python dependency management.
