#!/bin/bash

# GOSS Core Runner Launcher Script
# This script extracts and runs GOSS with Felix OSGi framework

set -e

GOSS_HOME="$(cd "$(dirname "$0")" && pwd)"
EXEC_DIR="$GOSS_HOME/generated/executable"
GOSS_JAR="$EXEC_DIR/goss-core-runner.jar"

echo "Starting GOSS Core Runner..."
echo "GOSS_HOME: $GOSS_HOME"

# Extract the executable JAR if not already extracted
cd "$EXEC_DIR"
if [ ! -d "bundle" ]; then
    echo "Extracting GOSS runtime..."
    jar xf "$GOSS_JAR"
fi

# Remove any extracted libraries that conflict with our bundles
echo "Cleaning up conflicts..."
rm -rf org META-INF com javax org.osgi.framework.* 2>/dev/null || true

# Create Felix config that avoids bundle conflicts
cat > config.properties << 'EOF'
# GOSS Core Runner Configuration for Felix OSGi Framework

# Basic Felix properties
felix.log.level=2
felix.cache.rootdir=felix-cache

# GOSS system properties
goss.activemq.host=0.0.0.0
goss.data=wunderdata
goss.openwire.port=61616
goss.broker-name=broker
goss.activemq.start.broker=true
goss.stomp.port=61613
goss.ws.port=61614

# Auto-install essential OSGi services first (start level 1)
felix.auto.start.1= \
file:bundle/org.apache.felix.scr-2.1.30.jar \
file:bundle/org.apache.felix.configadmin-1.9.24.jar \
file:bundle/slf4j-api-2.0.13.jar \
file:bundle/slf4j-simple-2.0.13.jar

# Auto-install third-party libraries (start level 2)  
felix.auto.start.2= \
file:bundle/gson-2.11.0.jar \
file:bundle/xstream-1.4.19.jar \
file:bundle/commons-io-2.11.0.jar \
file:bundle/commons-pool2-2.11.1.jar \
file:bundle/shiro-core-1.13.0.jar \
file:bundle/h2-2.1.214.jar

# Auto-install GOSS bundles (start level 3)
felix.auto.start.3= \
file:bundle/pnnl.goss.core.core-api.jar \
file:bundle/pnnl.goss.core.goss-core-exceptions.jar \
file:bundle/pnnl.goss.core.goss-core-security.jar \
file:bundle/pnnl.goss.core.goss-core-server-api.jar \
file:bundle/pnnl.goss.core.goss-core-server-registry.jar \
file:bundle/pnnl.goss.core.goss-core-server.jar \
file:bundle/pnnl.goss.core.goss-client.jar \
file:bundle/pnnl.goss.core.goss-core-commands.jar \
file:bundle/pnnl.goss.core.security-propertyfile.jar \
file:bundle/pnnl.goss.core.runner.jar

# ActiveMQ (start level 4 - after everything else)
felix.auto.start.4= \
file:bundle/activemq-osgi-5.15.16.jar

# Framework properties
felix.shutdown.hook=true
org.osgi.framework.system.packages.extra=sun.misc
EOF

# Run Felix
echo "Starting Felix OSGi framework..."
java -Dfelix.config.properties=file:config.properties \
     -Djava.util.logging.config.file=conf/logging.properties \
     -cp . \
     org.apache.felix.main.Main