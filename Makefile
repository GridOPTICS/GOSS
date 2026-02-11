# GOSS Makefile
# Provides version management and release automation

.PHONY: help version release snapshot build test itest clean push-snapshot-local push-release-local \
        bump-patch bump-minor bump-major next-snapshot check-api format format-check \
        run run-ssl stop status log

# Default target
help:
	@echo "GOSS Build and Release Management"
	@echo ""
	@echo "Usage:"
	@echo "  make version          Show versions of all bundles"
	@echo "  make release VERSION=x.y.z   Create a new release (removes -SNAPSHOT)"
	@echo "  make snapshot VERSION=x.y.z  Set version with -SNAPSHOT suffix"
	@echo "  make build            Build all bundles"
	@echo "  make test             Run tests"
	@echo "  make clean            Clean build artifacts"
	@echo "  make format           Format all Java files using Spotless"
	@echo "  make format-check     Check formatting without making changes"
	@echo ""
	@echo "Version bumping:"
	@echo "  make check-api        Analyze API changes and suggest version bump type"
	@echo "  make next-snapshot    Bump patch version after release (e.g., 11.0.0 -> 11.0.1-SNAPSHOT)"
	@echo "  make bump-patch       Same as next-snapshot"
	@echo "  make bump-minor       Bump minor version (e.g., 11.0.0 -> 11.1.0-SNAPSHOT)"
	@echo "  make bump-major       Bump major version (e.g., 11.0.0 -> 12.0.0-SNAPSHOT)"
	@echo ""
	@echo "Repository targets (local ../GOSS-Repository):"
	@echo "  make push-snapshot-local  Push snapshot JARs to ../GOSS-Repository/snapshot/"
	@echo "  make push-release-local   Push release JARs to ../GOSS-Repository/release/"
	@echo "  Add FORCE=1 to overwrite existing JARs (e.g., make push-release-local FORCE=1)"
	@echo ""
	@echo "Release workflow:"
	@echo "  1. make version                    # Check current version"
	@echo "  2. make release VERSION=11.0.0    # Set release version"
	@echo "  3. make build && make test        # Build and test"
	@echo "  4. make push-release-local        # Push to GOSS-Repository"
	@echo "  5. git tag v11.0.0 && git push    # Tag and push"
	@echo "  6. make next-snapshot             # Bump to next snapshot"
	@echo ""
	@echo "Integration testing:"
	@echo "  make itest            Build, start GOSS, run pixi integration tests, stop GOSS"
	@echo ""
	@echo "Running:"
	@echo "  make run              Build and run GOSS in the background (logs to log/goss.log)"
	@echo "  make run-ssl          Build and run GOSS with SSL in the background"
	@echo "  make stop             Stop the background GOSS process"
	@echo "  make status           Check if GOSS is running"
	@echo "  make log              Tail the GOSS log file"
	@echo ""
	@echo "Examples:"
	@echo "  make version"
	@echo "  make release VERSION=11.0.0"
	@echo "  make snapshot VERSION=11.1.0"
	@echo "  make build && make push-snapshot-local"

# Show all bundle versions
version:
	@python3 scripts/version.py show

# Create a release (remove -SNAPSHOT suffix)
release:
ifndef VERSION
	$(error VERSION is required. Usage: make release VERSION=x.y.z)
endif
	@python3 scripts/version.py release $(VERSION)

# Set snapshot version
snapshot:
ifndef VERSION
	$(error VERSION is required. Usage: make snapshot VERSION=x.y.z)
endif
	@python3 scripts/version.py snapshot $(VERSION)

# Build all bundles (compile + package, no tests)
build:
	./gradlew assemble

# Run tests
test:
	./gradlew check

# Clean build artifacts
clean:
	./gradlew clean

# Push snapshot JARs to local GOSS-Repository
push-snapshot-local:
	@python3 push-to-local-goss-repository.py --snapshot $(if $(FORCE),--force,)

# Push release JARs to local GOSS-Repository (also releases to cnf/releaserepo)
push-release-local:
	./gradlew release
	@python3 push-to-local-goss-repository.py --release $(if $(FORCE),--force,)

# Version bumping commands
bump-patch:
	@python3 scripts/version.py bump-patch

bump-minor:
	@python3 scripts/version.py bump-minor

bump-major:
	@python3 scripts/version.py bump-major

next-snapshot:
	@python3 scripts/version.py next-snapshot

# API change detection
check-api:
	@python3 scripts/check-api.py

# Code formatting targets (uses Spotless with Eclipse formatter)
format:
	@echo "Formatting Java files..."
	./gradlew spotlessApply
	@echo "Formatting complete."

format-check:
	@echo "Checking code formatting..."
	./gradlew spotlessCheck
	@echo "Format check complete."

# --- Integration test targets ---

ITESTS_DIR = pnnl.goss.core.itests
STOMP_PORT = 61618
GOSS_READY_TIMEOUT = 30

# Build GOSS, start it, run pixi integration tests, then stop GOSS
itest: $(SIMPLE_JAR)
	@mkdir -p $(LOG_DIR)
	@# Stop any existing instance
	@if [ -f $(PID_FILE) ] && kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "Stopping existing GOSS (PID $$(cat $(PID_FILE)))..."; \
		kill $$(cat $(PID_FILE)); \
		rm -f $(PID_FILE); \
		sleep 2; \
	fi
	@echo "Starting GOSS for integration tests (logging to $(LOG_FILE))..."
	@nohup java -jar $(SIMPLE_JAR) >> $(LOG_FILE) 2>&1 & echo $$! > $(PID_FILE)
	@echo "Waiting for GOSS STOMP port $(STOMP_PORT)..."
	@elapsed=0; \
	while ! ss -tln 2>/dev/null | grep -q ":$(STOMP_PORT) " && [ $$elapsed -lt $(GOSS_READY_TIMEOUT) ]; do \
		sleep 1; \
		elapsed=$$((elapsed + 1)); \
		printf "."; \
	done; \
	echo ""; \
	if ! ss -tln 2>/dev/null | grep -q ":$(STOMP_PORT) "; then \
		echo "ERROR: GOSS did not start within $(GOSS_READY_TIMEOUT)s"; \
		echo "Last log lines:"; \
		tail -20 $(LOG_FILE); \
		kill $$(cat $(PID_FILE)) 2>/dev/null; rm -f $(PID_FILE); \
		exit 1; \
	fi
	@echo "GOSS is ready (PID $$(cat $(PID_FILE)))"
	@echo ""
	@echo "Running pixi integration tests..."
	@cd $(ITESTS_DIR) && pixi run test-stomp-token; rc=$$?; \
	echo ""; \
	echo "Stopping GOSS..."; \
	kill $$(cat ../$(PID_FILE)) 2>/dev/null; rm -f ../$(PID_FILE); \
	exit $$rc

# --- Runtime targets ---

RUNNER_DIR = pnnl.goss.core.runner
SIMPLE_JAR = $(RUNNER_DIR)/generated/executable/goss-simple-runner.jar
SSL_JAR    = $(RUNNER_DIR)/generated/executable/goss-ssl-runner.jar
LOG_DIR    = log
LOG_FILE   = $(LOG_DIR)/goss.log
PID_FILE   = $(LOG_DIR)/goss.pid

# Build (if needed) and run GOSS in the background
run: $(SIMPLE_JAR)
	@mkdir -p $(LOG_DIR)
	@if [ -f $(PID_FILE) ] && kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "GOSS is already running (PID $$(cat $(PID_FILE))). Use 'make stop' first."; \
		exit 1; \
	fi
	@echo "Starting GOSS (logging to $(LOG_FILE))..."
	@nohup java -jar $(SIMPLE_JAR) >> $(LOG_FILE) 2>&1 & echo $$! > $(PID_FILE)
	@echo "GOSS started (PID $$(cat $(PID_FILE)))"

# Build (if needed) and run GOSS with SSL in the background
run-ssl: $(SSL_JAR)
	@mkdir -p $(LOG_DIR)
	@if [ -f $(PID_FILE) ] && kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "GOSS is already running (PID $$(cat $(PID_FILE))). Use 'make stop' first."; \
		exit 1; \
	fi
	@echo "Starting GOSS with SSL (logging to $(LOG_FILE))..."
	@nohup java -jar $(SSL_JAR) >> $(LOG_FILE) 2>&1 & echo $$! > $(PID_FILE)
	@echo "GOSS started with SSL (PID $$(cat $(PID_FILE)))"

# Build the runner JARs if they don't exist
$(SIMPLE_JAR):
	./gradlew :pnnl.goss.core.runner:createSimpleRunner

$(SSL_JAR):
	./gradlew :pnnl.goss.core.runner:createSSLRunner

# Stop the background GOSS process
stop:
	@if [ -f $(PID_FILE) ] && kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "Stopping GOSS (PID $$(cat $(PID_FILE)))..."; \
		kill $$(cat $(PID_FILE)); \
		rm -f $(PID_FILE); \
		echo "GOSS stopped."; \
	else \
		echo "GOSS is not running."; \
		rm -f $(PID_FILE); \
	fi

# Check if GOSS is running
status:
	@if [ -f $(PID_FILE) ] && kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "GOSS is running (PID $$(cat $(PID_FILE)))"; \
	else \
		echo "GOSS is not running."; \
		rm -f $(PID_FILE); \
	fi

# Tail the GOSS log
log:
	@if [ -f $(LOG_FILE) ]; then \
		tail -f $(LOG_FILE); \
	else \
		echo "No log file found at $(LOG_FILE)"; \
	fi
