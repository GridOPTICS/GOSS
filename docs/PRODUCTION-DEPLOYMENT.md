# GOSS Production Deployment Guide

This guide covers deploying GOSS (GridOPTICS Software System) in production environments using the executable JARs.

## Deployment Options

GOSS provides two deployment options:

1. **Simple Runner** (`goss-simple-runner.jar`) - 33MB, core functionality
2. **Full OSGi Runner** (`goss-core-runner.jar`) - 80MB, complete framework

## System Requirements

### Hardware Requirements (Minimum)

- **CPU**: 2 cores, 2.0 GHz
- **RAM**: 2 GB (4 GB recommended)
- **Storage**: 10 GB available space
- **Network**: 1 Gbps network interface (for high-throughput messaging)

### Hardware Requirements (Recommended)

- **CPU**: 4+ cores, 3.0 GHz
- **RAM**: 8 GB (16 GB for high load)
- **Storage**: 50 GB SSD (for message persistence)
- **Network**: 10 Gbps network interface

### Software Requirements

- **Operating System**: Linux (Ubuntu 20.04+, RHEL 8+, CentOS 8+), Windows Server 2019+, macOS 12+
- **Java Runtime**: OpenJDK 21 or Oracle JDK 21
- **User Account**: Non-root user with sudo privileges (recommended)

## Pre-Deployment Setup

### 1. Install Java 21

#### Ubuntu/Debian

```bash
sudo apt update
sudo apt install openjdk-21-jre-headless

# Verify installation
java -version
```

#### RHEL/CentOS/Rocky Linux

```bash
# Enable EPEL repository if needed
sudo dnf install epel-release

# Install Java 21
sudo dnf install java-21-openjdk-headless

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk' >> ~/.bashrc
source ~/.bashrc
```

#### Windows Server

1. Download OpenJDK 21 from [Eclipse Adoptium](https://adoptium.net/)
2. Install using the MSI installer
3. Set `JAVA_HOME` environment variable
4. Add `%JAVA_HOME%\bin` to system PATH

### 2. Create GOSS User (Linux/macOS)

```bash
# Create dedicated user for GOSS
sudo useradd -r -m -s /bin/bash goss
sudo usermod -aG sudo goss

# Create application directories
sudo mkdir -p /opt/goss/{bin,conf,data,logs}
sudo chown -R goss:goss /opt/goss
```

### 3. Firewall Configuration

#### Linux (UFW)

```bash
# Allow GOSS ports
sudo ufw allow 61617/tcp  # ActiveMQ OpenWire
sudo ufw allow 61618/tcp  # ActiveMQ STOMP
sudo ufw allow 8080/tcp   # HTTP/REST API (if enabled)
sudo ufw allow 8443/tcp   # HTTPS/REST API (if enabled)

# Apply rules
sudo ufw reload
```

#### Linux (firewalld)

```bash
# Add GOSS ports
sudo firewall-cmd --permanent --add-port=61617/tcp
sudo firewall-cmd --permanent --add-port=61618/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8443/tcp

# Reload configuration
sudo firewall-cmd --reload
```

#### Windows

```powershell
# Open Windows Firewall with Advanced Security
# Add inbound rules for ports 61617, 61618, 8080, 8443
New-NetFirewallRule -DisplayName "GOSS-ActiveMQ" -Direction Inbound -Port 61617 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "GOSS-STOMP" -Direction Inbound -Port 61618 -Protocol TCP -Action Allow
```

## Simple Runner Deployment

### 1. Deploy the JAR

#### Linux/macOS

```bash
# Switch to goss user
sudo su - goss

# Copy JAR to deployment directory
cp /path/to/goss-simple-runner.jar /opt/goss/bin/

# Make executable (optional, for convenience)
chmod +x /opt/goss/bin/goss-simple-runner.jar
```

#### Windows

```batch
REM Copy JAR to application directory
copy C:\path\to\goss-simple-runner.jar "C:\Program Files\GOSS\bin\"
```

### 2. Create Configuration Files

#### Application Configuration (`/opt/goss/conf/goss.properties`)

```properties
# GOSS Simple Runner Configuration

# ActiveMQ Broker Settings
activemq.host=0.0.0.0
activemq.openwire.port=61617
activemq.stomp.port=61618
activemq.broker.name=goss-production-broker

# Data Storage
data.directory=/opt/goss/data
log.directory=/opt/goss/logs

# Memory Settings (MB)
activemq.memory.limit=512
activemq.store.limit=10240

# Security Settings
security.enabled=false
# security.realm=property-file
# security.property.file=/opt/goss/conf/users.properties

# Performance Settings
activemq.persistent=true
activemq.advisory.support=false
activemq.statistics.broker=true
```

#### Logging Configuration (`/opt/goss/conf/logging.properties`)

```properties
# GOSS Logging Configuration

# Root logger
.level = INFO
handlers = java.util.logging.FileHandler, java.util.logging.ConsoleHandler

# File handler
java.util.logging.FileHandler.level = INFO
java.util.logging.FileHandler.pattern = /opt/goss/logs/goss-%g.log
java.util.logging.FileHandler.count = 5
java.util.logging.FileHandler.limit = 10485760
java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter

# Console handler
java.util.logging.ConsoleHandler.level = WARNING
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter

# GOSS specific loggers
pnnl.goss.level = INFO
org.apache.activemq.level = INFO
org.apache.shiro.level = INFO

# Suppress verbose logging
org.apache.activemq.transport.level = WARNING
org.apache.activemq.broker.region.level = WARNING
```

#### Users Configuration (if security enabled) (`/opt/goss/conf/users.properties`)

```properties
# GOSS Users Configuration
# Format: username=password,role1,role2

# Admin users
admin=admin_password,admin,user
operator=operator_password,operator,user

# Regular users
user1=user1_password,user
user2=user2_password,user

# Roles definition
# admin: Full system access
# operator: Can manage queues and topics
# user: Can send/receive messages
```

### 3. Create Startup Scripts

#### Linux Systemd Service (`/etc/systemd/system/goss.service`)

```ini
[Unit]
Description=GOSS (GridOPTICS Software System) Message Broker
After=network.target

[Service]
Type=simple
User=goss
Group=goss
WorkingDirectory=/opt/goss
ExecStart=/usr/bin/java -Xmx1g -Xms512m \
    -Djava.util.logging.config.file=/opt/goss/conf/logging.properties \
    -Dgoss.config.file=/opt/goss/conf/goss.properties \
    -jar /opt/goss/bin/goss-simple-runner.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

# Security settings
NoNewPrivileges=yes
PrivateTmp=yes
ProtectSystem=strict
ProtectHome=yes
ReadWritePaths=/opt/goss/data /opt/goss/logs

[Install]
WantedBy=multi-user.target
```

#### Linux SysV Init Script (`/etc/init.d/goss`)

```bash
#!/bin/bash
# GOSS        GOSS Message Broker
# chkconfig: 35 80 20
# description: GOSS Message Broker Service

. /etc/rc.d/init.d/functions

USER="goss"
DAEMON="goss"
ROOT_DIR="/opt/goss"
JAVA_HOME="/usr/lib/jvm/java-21-openjdk"

SERVER="$ROOT_DIR/bin/goss-simple-runner.jar"
LOCK_FILE="/var/lock/subsys/goss"

start() {
    echo -n "Starting $DAEMON: "
    pid=$(ps -aefw | grep "$DAEMON" | grep -v " grep " | awk '{print $2}')
    [ -n "$pid" ] && echo "$DAEMON is already running [$pid]" && exit 1

    daemon --user "$USER" --pidfile="$LOCK_FILE" \
        $JAVA_HOME/bin/java -Xmx1g -Xms512m \
        -Djava.util.logging.config.file="$ROOT_DIR/conf/logging.properties" \
        -Dgoss.config.file="$ROOT_DIR/conf/goss.properties" \
        -jar "$SERVER" &

    RETVAL=$?
    echo
    [ $RETVAL -eq 0 ] && touch $LOCK_FILE
    return $RETVAL
}

stop() {
    echo -n "Shutting down $DAEMON: "
    pid=$(ps -aefw | grep "$DAEMON" | grep -v " grep " | awk '{print $2}')
    [ -n "$pid" ] && kill $pid && echo "[$pid]" && rm -f $LOCK_FILE
    [ ! -n "$pid" ] && echo "not running"
}

status() {
    pid=$(ps -aefw | grep "$DAEMON" | grep -v " grep " | awk '{print $2}')
    [ -n "$pid" ] && echo "$DAEMON is running [$pid]"
    [ ! -n "$pid" ] && echo "$DAEMON is stopped"
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    status)
        status
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
esac

exit $?
```

#### Windows Service (using NSSM)

```batch
REM Download and install NSSM (Non-Sucking Service Manager)
REM https://nssm.cc/download

REM Install GOSS as Windows Service
nssm install GOSS "C:\Program Files\Java\jdk-21\bin\java.exe"
nssm set GOSS Parameters -Xmx1g -Xms512m -Djava.util.logging.config.file="C:\Program Files\GOSS\conf\logging.properties" -jar "C:\Program Files\GOSS\bin\goss-simple-runner.jar"
nssm set GOSS AppDirectory "C:\Program Files\GOSS"
nssm set GOSS DisplayName "GOSS Message Broker"
nssm set GOSS Description "GridOPTICS Software System Message Broker"
nssm set GOSS Start SERVICE_AUTO_START

REM Start the service
net start GOSS
```

### 4. Start and Enable Service

#### Systemd (Ubuntu/RHEL/CentOS)

```bash
# Reload systemd configuration
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable goss

# Start the service
sudo systemctl start goss

# Check status
sudo systemctl status goss

# View logs
sudo journalctl -u goss -f
```

#### SysV Init

```bash
# Make script executable
sudo chmod +x /etc/init.d/goss

# Enable service
sudo chkconfig goss on

# Start service
sudo service goss start

# Check status
sudo service goss status
```

## SSL/TLS Configuration (Secure Deployment)

### 1. Generate SSL Certificates

#### Using OpenSSL (Self-Signed for Testing)

```bash
# Create certificate directory
mkdir -p /opt/goss/ssl

# Generate private key
openssl genrsa -out /opt/goss/ssl/goss-server.key 2048

# Generate certificate signing request
openssl req -new -key /opt/goss/ssl/goss-server.key \
    -out /opt/goss/ssl/goss-server.csr \
    -subj "/CN=goss.yourdomain.com/O=Your Organization/C=US"

# Generate self-signed certificate (valid for 1 year)
openssl x509 -req -days 365 \
    -in /opt/goss/ssl/goss-server.csr \
    -signkey /opt/goss/ssl/goss-server.key \
    -out /opt/goss/ssl/goss-server.crt

# Create Java keystore
keytool -import -alias goss-server \
    -file /opt/goss/ssl/goss-server.crt \
    -keystore /opt/goss/ssl/goss-keystore.jks \
    -storepass changeit -noprompt

# Set permissions
chown -R goss:goss /opt/goss/ssl
chmod 600 /opt/goss/ssl/goss-server.key
```

### 2. Configure SSL in GOSS

Update `/opt/goss/conf/goss.properties`:

```properties
# Enable SSL
ssl.enabled=true
ssl.port=61443
ssl.keystore.path=/opt/goss/ssl/goss-keystore.jks
ssl.keystore.password=changeit
ssl.truststore.path=/opt/goss/ssl/goss-keystore.jks
ssl.truststore.password=changeit

# Disable non-SSL ports (optional)
# activemq.openwire.port=
# activemq.stomp.port=
```

## Monitoring and Maintenance

### 1. Health Check Scripts

#### Linux Health Check (`/opt/goss/bin/health-check.sh`)

```bash
#!/bin/bash

# GOSS Health Check Script

GOSS_HOST="localhost"
GOSS_PORT="61617"
LOG_FILE="/opt/goss/logs/health-check.log"
DATE=$(date "+%Y-%m-%d %H:%M:%S")

# Function to log messages
log_message() {
    echo "[$DATE] $1" | tee -a "$LOG_FILE"
}

# Check if GOSS process is running
if ! pgrep -f "goss-simple-runner.jar" > /dev/null; then
    log_message "ERROR: GOSS process is not running"
    exit 1
fi

# Check if GOSS port is listening
if ! netstat -tln | grep ":$GOSS_PORT " > /dev/null; then
    log_message "ERROR: GOSS is not listening on port $GOSS_PORT"
    exit 1
fi

# Check TCP connectivity
if ! nc -z "$GOSS_HOST" "$GOSS_PORT"; then
    log_message "ERROR: Cannot connect to GOSS on $GOSS_HOST:$GOSS_PORT"
    exit 1
fi

log_message "SUCCESS: GOSS is healthy"
exit 0
```

#### Windows Health Check (`health-check.bat`)

```batch
@echo off
set GOSS_HOST=localhost
set GOSS_PORT=61617
set LOG_FILE=C:\Program Files\GOSS\logs\health-check.log

echo [%date% %time%] Starting GOSS health check >> %LOG_FILE%

REM Check if GOSS service is running
sc query GOSS | find "RUNNING" >nul
if %errorlevel% neq 0 (
    echo [%date% %time%] ERROR: GOSS service is not running >> %LOG_FILE%
    exit /b 1
)

REM Check if port is listening
netstat -an | find ":%GOSS_PORT%" >nul
if %errorlevel% neq 0 (
    echo [%date% %time%] ERROR: GOSS is not listening on port %GOSS_PORT% >> %LOG_FILE%
    exit /b 1
)

echo [%date% %time%] SUCCESS: GOSS is healthy >> %LOG_FILE%
exit /b 0
```

### 2. Log Rotation

#### Linux (logrotate)

Create `/etc/logrotate.d/goss`:

```
/opt/goss/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    copytruncate
    postrotate
        systemctl reload goss
    endscript
}
```

### 3. Monitoring Integration

#### Prometheus Metrics (if enabled)

GOSS can expose metrics for Prometheus monitoring:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: "goss"
    static_configs:
      - targets: ["goss-server:8080"]
    metrics_path: "/metrics"
    scrape_interval: 15s
```

#### Nagios/Icinga Check

```bash
#!/bin/bash
# /usr/local/nagios/libexec/check_goss.sh

/opt/goss/bin/health-check.sh
exit $?
```

## Performance Tuning

### 1. JVM Tuning

For high-throughput environments, update the systemd service:

```ini
ExecStart=/usr/bin/java -Xmx4g -Xms2g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/opt/goss/logs \
    -Djava.util.logging.config.file=/opt/goss/conf/logging.properties \
    -jar /opt/goss/bin/goss-simple-runner.jar
```

### 2. Operating System Tuning

#### Linux

```bash
# Increase file descriptor limits
echo "goss soft nofile 65536" >> /etc/security/limits.conf
echo "goss hard nofile 65536" >> /etc/security/limits.conf

# TCP tuning for high throughput
echo 'net.core.rmem_max = 16777216' >> /etc/sysctl.conf
echo 'net.core.wmem_max = 16777216' >> /etc/sysctl.conf
echo 'net.ipv4.tcp_rmem = 4096 12582912 16777216' >> /etc/sysctl.conf
echo 'net.ipv4.tcp_wmem = 4096 12582912 16777216' >> /etc/sysctl.conf

# Apply changes
sysctl -p
```

## Backup and Recovery

### 1. Backup Strategy

#### Data Directory Backup

```bash
#!/bin/bash
# /opt/goss/bin/backup.sh

BACKUP_DIR="/opt/goss/backups"
DATE=$(date "+%Y%m%d_%H%M%S")
BACKUP_FILE="goss_backup_$DATE.tar.gz"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Stop GOSS service
systemctl stop goss

# Create backup
tar -czf "$BACKUP_DIR/$BACKUP_FILE" \
    -C /opt/goss \
    data conf logs

# Start GOSS service
systemctl start goss

# Keep only last 7 backups
find "$BACKUP_DIR" -name "goss_backup_*.tar.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_DIR/$BACKUP_FILE"
```

### 2. Recovery Procedure

```bash
#!/bin/bash
# Recovery script

BACKUP_FILE="/opt/goss/backups/goss_backup_YYYYMMDD_HHMMSS.tar.gz"

# Stop GOSS service
systemctl stop goss

# Backup current state (just in case)
tar -czf "/opt/goss/backups/pre_recovery_$(date +%Y%m%d_%H%M%S).tar.gz" \
    -C /opt/goss data conf logs

# Restore from backup
tar -xzf "$BACKUP_FILE" -C /opt/goss

# Set permissions
chown -R goss:goss /opt/goss

# Start GOSS service
systemctl start goss

echo "Recovery completed from $BACKUP_FILE"
```

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

```bash
# Check what's using the port
sudo netstat -tlnp | grep 61617
# or
sudo ss -tlnp | grep 61617

# Change port in configuration if needed
```

#### 2. Out of Memory Errors

```bash
# Check Java heap dump
ls -la /opt/goss/logs/*.hprof

# Increase heap size in systemd service
# -Xmx4g -Xms2g
```

#### 3. Permission Denied Errors

```bash
# Fix permissions
sudo chown -R goss:goss /opt/goss
sudo chmod -R 755 /opt/goss
sudo chmod 600 /opt/goss/ssl/*
```

#### 4. SSL Certificate Issues

```bash
# Verify certificate
openssl x509 -in /opt/goss/ssl/goss-server.crt -text -noout

# Test SSL connection
openssl s_client -connect localhost:61443
```

### Getting Support

1. **Check logs**: `/opt/goss/logs/`
2. **Run health check**: `/opt/goss/bin/health-check.sh`
3. **Review configuration**: `/opt/goss/conf/`
4. **System resources**: `htop`, `free -h`, `df -h`

## Security Best Practices

1. **Use SSL/TLS** for all production deployments
2. **Enable authentication** with strong passwords
3. **Run as non-root user** (goss user)
4. **Keep Java updated** for security patches
5. **Regular backups** of configuration and data
6. **Monitor logs** for security events
7. **Network segmentation** - restrict access to GOSS ports
8. **Regular security updates** for the operating system

## Scaling and High Availability

For enterprise deployments requiring high availability:

1. **Load Balancer**: Use HAProxy or NGINX to distribute connections
2. **Cluster Setup**: Multiple GOSS instances with shared storage
3. **Database Backend**: Use PostgreSQL/MySQL for persistent message storage
4. **Container Deployment**: Docker/Kubernetes deployment options
5. **Message Replication**: Configure ActiveMQ master-slave setup

See the [ENTERPRISE-DEPLOYMENT.md](ENTERPRISE-DEPLOYMENT.md) guide for advanced deployment scenarios.
