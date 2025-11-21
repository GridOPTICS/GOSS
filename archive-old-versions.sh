#!/bin/bash
#
# Archive old GOSS versions
# Keeps only the last N major versions, archives the rest
#

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Configuration
MAJOR_VERSIONS_TO_KEEP=3
ARCHIVE_DIR="../GOSS-Repository/cnf/releaserepo"

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

# Extract major version from JAR filename
get_major_version() {
    local jar_file="$1"
    # Extract version like "11.0.0" from "pnnl.goss.core.core-api-11.0.0.jar"
    local version=$(echo "$jar_file" | sed -E 's/.*-([0-9]+\.[0-9]+\.[0-9]+)\.jar$/\1/')
    # Get major version
    echo "$version" | cut -d. -f1
}

log_info "Analyzing GOSS release repository..."

# Find all JARs and their major versions
declare -A version_counts
declare -a all_major_versions

while IFS= read -r jar_file; do
    if [ -f "$jar_file" ]; then
        major_version=$(get_major_version "$(basename "$jar_file")")
        if [[ "$major_version" =~ ^[0-9]+$ ]]; then
            version_counts[$major_version]=$((${version_counts[$major_version]:-0} + 1))
            all_major_versions+=($major_version)
        fi
    fi
done < <(find cnf/releaserepo -name "*.jar" -type f)

# Get unique sorted major versions (newest first)
major_versions=($(printf '%s\n' "${all_major_versions[@]}" | sort -run))

log_info "Found ${#major_versions[@]} major versions in repository"

# Display version summary
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Major Version Summary:${NC}"
for version in "${major_versions[@]}"; do
    count=${version_counts[$version]}
    echo -e "  Version ${BLUE}${version}.x${NC}: $count JARs"
done
echo -e "${BLUE}========================================${NC}"
echo ""

# Determine which versions to keep and which to archive
versions_to_keep=("${major_versions[@]:0:$MAJOR_VERSIONS_TO_KEEP}")
versions_to_archive=("${major_versions[@]:$MAJOR_VERSIONS_TO_KEEP}")

if [ ${#versions_to_archive[@]} -eq 0 ]; then
    log_info "No old versions to archive. All versions are within the keep threshold."
    exit 0
fi

echo -e "${GREEN}Versions to keep:${NC}"
for version in "${versions_to_keep[@]}"; do
    count=${version_counts[$version]}
    echo -e "  ✓ ${GREEN}${version}.x${NC} ($count JARs)"
done
echo ""

echo -e "${YELLOW}Versions to archive:${NC}"
total_to_archive=0
for version in "${versions_to_archive[@]}"; do
    count=${version_counts[$version]}
    total_to_archive=$((total_to_archive + count))
    echo -e "  → ${YELLOW}${version}.x${NC} ($count JARs)"
done
echo ""

log_info "Total JARs to archive: $total_to_archive"

# Ask for confirmation
read -p "Archive old versions to $ARCHIVE_DIR? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_warn "Archive cancelled"
    exit 0
fi

# Create archive directory structure
log_info "Creating archive directory: $ARCHIVE_DIR"
mkdir -p "$ARCHIVE_DIR"

# Archive old versions
archived_count=0
removed_count=0

log_info "Archiving old versions..."

for jar_file in cnf/releaserepo/*/*.jar; do
    if [ ! -f "$jar_file" ]; then
        continue
    fi

    jar_name=$(basename "$jar_file")
    major_version=$(get_major_version "$jar_name")

    # Check if this version should be archived
    should_archive=false
    for archive_version in "${versions_to_archive[@]}"; do
        if [ "$major_version" == "$archive_version" ]; then
            should_archive=true
            break
        fi
    done

    if [ "$should_archive" = true ]; then
        # Get relative path from releaserepo
        rel_path=${jar_file#cnf/releaserepo/}
        archive_path="$ARCHIVE_DIR/$rel_path"

        # Create directory structure in archive
        mkdir -p "$(dirname "$archive_path")"

        # Copy to archive
        cp "$jar_file" "$archive_path"
        archived_count=$((archived_count + 1))

        # Remove from current repo
        rm "$jar_file"
        removed_count=$((removed_count + 1))

        # Progress indicator
        if [ $((archived_count % 50)) -eq 0 ]; then
            log_info "Progress: $archived_count JARs archived..."
        fi
    fi
done

# Clean up empty directories
find cnf/releaserepo -type d -empty -delete

# Update repository index
log_info "Updating repository index..."
./gradlew :pnnl.goss.core:release > /dev/null 2>&1 || true

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Archive Complete!${NC}"
echo -e "  Archived:  ${GREEN}$archived_count${NC} JARs → $ARCHIVE_DIR"
echo -e "  Removed:   ${YELLOW}$removed_count${NC} JARs from current repo"
echo -e "  Kept:      ${BLUE}$(find cnf/releaserepo -name '*.jar' | wc -l)${NC} JARs (versions ${versions_to_keep[*]})"
echo -e "${GREEN}========================================${NC}"
echo ""

log_info "${GREEN}✓ Done!${NC}"
log_info ""
log_info "Next steps:"
log_info "  1. Review archived JARs: ls $ARCHIVE_DIR"
log_info "  2. Stage changes: git add cnf/releaserepo/"
log_info "  3. Create GOSS-Repository: cd .. && git init GOSS-Repository"
log_info "  4. Commit archive: cd GOSS-Repository && git add . && git commit -m 'Archive GOSS versions ${versions_to_archive[*]}'"
