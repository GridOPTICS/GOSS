# GOSS Makefile
# Provides version management and release automation

.PHONY: help version release snapshot build test clean push-snapshot-local push-release-local \
        bump-patch bump-minor bump-major next-snapshot check-api format format-check

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

# Build all bundles
build:
	./gradlew build

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
