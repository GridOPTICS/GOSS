#!/bin/bash
#
# GOSS Release Script
# Usage: ./release.sh [release|snapshot] [version]
#
# Examples:
#   ./release.sh release          # Remove -SNAPSHOT from all versions
#   ./release.sh snapshot         # Add -SNAPSHOT to all versions
#   ./release.sh release 11.0.0   # Set specific release version
#   ./release.sh snapshot 11.0.1  # Set specific snapshot version

set -e  # Exit on error

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to find all .bnd files
find_bnd_files() {
    find . -name "*.bnd" -type f | grep -v ".gradle" | grep -v "build/"
}

# Function to find all packageinfo files
find_packageinfo_files() {
    find . -name "packageinfo" -type f | grep -v "build/"
}

# Function to get current version from a file
get_current_version() {
    local file=$1
    grep "Bundle-Version:\|^version" "$file" | head -1 | sed 's/.*: *//' | sed 's/version *//'
}

# Function to update version in a file
update_version() {
    local file=$1
    local new_version=$2
    local current_version=$(get_current_version "$file")

    if [[ "$file" == *"packageinfo"* ]]; then
        sed -i "s/version .*/version $new_version/" "$file"
    else
        sed -i "s/Bundle-Version: .*/Bundle-Version: $new_version/" "$file"
    fi

    log_info "  Updated: $file ($current_version -> $new_version)"
}

# Function to remove -SNAPSHOT suffix
remove_snapshot() {
    local version=$1
    echo "$version" | sed 's/-SNAPSHOT//'
}

# Function to add -SNAPSHOT suffix
add_snapshot() {
    local version=$1
    if [[ "$version" == *"-SNAPSHOT" ]]; then
        echo "$version"
    else
        echo "$version-SNAPSHOT"
    fi
}

# Function to bump version (e.g., 11.0.0 -> 11.0.1)
bump_patch_version() {
    local version=$1
    # Remove -SNAPSHOT if present
    version=$(remove_snapshot "$version")

    # Split version into parts
    local major=$(echo "$version" | cut -d. -f1)
    local minor=$(echo "$version" | cut -d. -f2)
    local patch=$(echo "$version" | cut -d. -f3)

    # Increment patch version
    patch=$((patch + 1))

    echo "$major.$minor.$patch"
}

# Main logic
MODE=${1:-help}
TARGET_VERSION=$2

case "$MODE" in
    release)
        log_info "Removing -SNAPSHOT from all versions..."

        # Update .bnd files
        for file in $(find_bnd_files); do
            current_version=$(get_current_version "$file")
            new_version=$(remove_snapshot "$current_version")

            if [[ -n "$TARGET_VERSION" ]]; then
                new_version="$TARGET_VERSION"
            fi

            if [[ "$current_version" != "$new_version" ]]; then
                update_version "$file" "$new_version"
            fi
        done

        # Update packageinfo files
        for file in $(find_packageinfo_files); do
            current_version=$(get_current_version "$file")
            new_version=$(remove_snapshot "$current_version")

            if [[ -n "$TARGET_VERSION" ]]; then
                new_version="$TARGET_VERSION"
            fi

            if [[ "$current_version" != "$new_version" ]]; then
                update_version "$file" "$new_version"
            fi
        done

        log_info "Building release version..."
        ./gradlew clean build -x test

        log_info "${GREEN}✓ Release build complete!${NC}"
        ;;

    snapshot)
        log_info "Adding -SNAPSHOT to all versions..."

        # Update .bnd files
        for file in $(find_bnd_files); do
            current_version=$(get_current_version "$file")

            if [[ -n "$TARGET_VERSION" ]]; then
                new_version=$(add_snapshot "$TARGET_VERSION")
            else
                # If no target version, just add SNAPSHOT to current
                new_version=$(add_snapshot "$current_version")
            fi

            if [[ "$current_version" != "$new_version" ]]; then
                update_version "$file" "$new_version"
            fi
        done

        # Update packageinfo files
        for file in $(find_packageinfo_files); do
            current_version=$(get_current_version "$file")

            if [[ -n "$TARGET_VERSION" ]]; then
                new_version=$(add_snapshot "$TARGET_VERSION")
            else
                new_version=$(add_snapshot "$current_version")
            fi

            if [[ "$current_version" != "$new_version" ]]; then
                update_version "$file" "$new_version"
            fi
        done

        log_info "${GREEN}✓ Version updated to SNAPSHOT${NC}"
        ;;

    bump)
        log_info "Bumping version and adding -SNAPSHOT..."

        # Get first version to determine base version
        first_file=$(find_bnd_files | head -1)
        base_version=$(get_current_version "$first_file")
        base_version=$(remove_snapshot "$base_version")
        new_version=$(bump_patch_version "$base_version")
        new_version=$(add_snapshot "$new_version")

        log_info "Bumping version: $base_version -> $new_version"

        # Update all files
        for file in $(find_bnd_files); do
            update_version "$file" "$new_version"
        done

        for file in $(find_packageinfo_files); do
            update_version "$file" "$new_version"
        done

        log_info "${GREEN}✓ Version bumped to $new_version${NC}"
        ;;

    help|*)
        echo "GOSS Release Script"
        echo ""
        echo "Usage: ./release.sh [command] [version]"
        echo ""
        echo "Commands:"
        echo "  release          Remove -SNAPSHOT from all versions and build"
        echo "  release [ver]    Set all versions to specified release version and build"
        echo "  snapshot         Add -SNAPSHOT to all versions"
        echo "  snapshot [ver]   Set all versions to specified snapshot version"
        echo "  bump             Increment patch version and add -SNAPSHOT"
        echo "  help             Show this help message"
        echo ""
        echo "Examples:"
        echo "  ./release.sh release              # 11.0.0-SNAPSHOT -> 11.0.0"
        echo "  ./release.sh release 11.1.0       # Set all to 11.1.0"
        echo "  ./release.sh snapshot             # 11.0.0 -> 11.0.0-SNAPSHOT"
        echo "  ./release.sh snapshot 11.0.1      # Set all to 11.0.1-SNAPSHOT"
        echo "  ./release.sh bump                 # 11.0.0 -> 11.0.1-SNAPSHOT"
        ;;
esac
