#!/bin/bash
#
# Restore JARs from master branch to current branch
# Safely brings back historical releases without overwriting existing files
#

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in a git repo
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    log_error "Not in a git repository"
    exit 1
fi

# Get list of JARs in master
log_info "Analyzing JARs in master branch..."
MASTER_JARS=$(git ls-tree -r origin/master --name-only cnf/releaserepo/ | grep "\.jar$" | sort)
MASTER_COUNT=$(echo "$MASTER_JARS" | wc -l)
log_info "Found $MASTER_COUNT JARs in master"

# Get list of JARs in current branch
log_info "Analyzing JARs in current branch..."
CURRENT_JARS=$(git ls-tree -r HEAD --name-only cnf/releaserepo/ | grep "\.jar$" | sort)
CURRENT_COUNT=$(echo "$CURRENT_JARS" | wc -l)
log_info "Found $CURRENT_COUNT JARs in current branch"

# Find JARs to restore (in master but not in current)
log_info "Calculating JARs to restore..."
JARS_TO_RESTORE=$(comm -13 <(echo "$CURRENT_JARS") <(echo "$MASTER_JARS"))
RESTORE_COUNT=$(echo "$JARS_TO_RESTORE" | grep -v '^$' | wc -l)

log_info "${BLUE}JARs to restore: $RESTORE_COUNT${NC}"

if [ "$RESTORE_COUNT" -eq 0 ]; then
    log_info "No JARs to restore. Current branch has all JARs from master."
    exit 0
fi

# Show summary
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Summary:${NC}"
echo -e "  Master branch:   $MASTER_COUNT JARs"
echo -e "  Current branch:  $CURRENT_COUNT JARs"
echo -e "  To restore:      $RESTORE_COUNT JARs"
echo -e "${BLUE}========================================${NC}"
echo ""

# Ask for confirmation
read -p "Restore these JARs from master? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_warn "Restore cancelled"
    exit 0
fi

# Create temporary directory
TEMP_DIR=$(mktemp -d)
log_info "Using temporary directory: $TEMP_DIR"

# Track progress
RESTORED=0
FAILED=0

log_info "Restoring JARs from master..."

while IFS= read -r jar_path; do
    if [ -z "$jar_path" ]; then
        continue
    fi

    # Get the JAR from master branch
    if git show "origin/master:$jar_path" > "$TEMP_DIR/temp.jar" 2>/dev/null; then
        # Create directory if needed
        mkdir -p "$(dirname "$jar_path")"

        # Copy the JAR
        mv "$TEMP_DIR/temp.jar" "$jar_path"

        RESTORED=$((RESTORED + 1))

        # Show progress every 50 JARs
        if [ $((RESTORED % 50)) -eq 0 ]; then
            log_info "Progress: $RESTORED/$RESTORE_COUNT JARs restored..."
        fi
    else
        log_warn "Failed to restore: $jar_path"
        FAILED=$((FAILED + 1))
    fi
done <<< "$JARS_TO_RESTORE"

# Cleanup
rm -rf "$TEMP_DIR"

# Final summary
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Restore Complete!${NC}"
echo -e "  Restored:  ${GREEN}$RESTORED${NC} JARs"
if [ $FAILED -gt 0 ]; then
    echo -e "  Failed:    ${RED}$FAILED${NC} JARs"
fi
echo -e "${GREEN}========================================${NC}"
echo ""

log_info "Updating repository index..."
./gradlew :pnnl.goss.core:release

log_info "${GREEN}✓ All done!${NC}"
log_info "Run 'git status' to see the restored JARs"
log_info "Run 'git add cnf/releaserepo/' to stage them"
