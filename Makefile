# GOSS Makefile
# Provides version management and release automation

.PHONY: help version release snapshot build test clean push-snapshot push-release

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
	@echo ""
	@echo "Repository targets (local ../GOSS-Repository):"
	@echo "  make push-snapshot    Push snapshot JARs to ../GOSS-Repository/snapshot/"
	@echo "  make push-release     Push release JARs to ../GOSS-Repository/release/"
	@echo ""
	@echo "Examples:"
	@echo "  make version"
	@echo "  make release VERSION=11.0.0"
	@echo "  make snapshot VERSION=11.1.0"
	@echo "  make build && make push-snapshot"

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

# Push snapshot JARs to GOSS-Repository
push-snapshot:
	@python3 push-to-local-goss-repository.py --snapshot

# Push release JARs to GOSS-Repository
push-release:
	@python3 push-to-local-goss-repository.py --release
