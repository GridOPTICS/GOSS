#!/usr/bin/env python3
"""
Push GOSS artifacts to GOSS-Repository
Copies JARs from build output to the specified GOSS-Repository (release or snapshot)
"""

import argparse
import hashlib
import os
import re
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path


# ANSI Colors
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    CYAN = '\033[0;36m'
    NC = '\033[0m'  # No Color


def log_info(msg: str) -> None:
    print(f"{Colors.GREEN}[INFO]{Colors.NC} {msg}")


def log_warn(msg: str) -> None:
    print(f"{Colors.YELLOW}[WARN]{Colors.NC} {msg}")


def log_error(msg: str) -> None:
    print(f"{Colors.RED}[ERROR]{Colors.NC} {msg}")


def extract_bundle_version(jar_path: Path) -> str | None:
    """Extract Bundle-Version from JAR manifest."""
    try:
        with zipfile.ZipFile(jar_path, 'r') as zf:
            manifest_data = zf.read('META-INF/MANIFEST.MF').decode('utf-8')
    except (zipfile.BadZipFile, KeyError, IOError):
        return None

    # Parse manifest (handle line continuations)
    lines = manifest_data.replace('\r\n ', '').replace('\r\n\t', '').split('\r\n')
    if len(lines) == 1:
        lines = manifest_data.replace('\n ', '').replace('\n\t', '').split('\n')

    for line in lines:
        if line.startswith('Bundle-Version:'):
            return line.split(':', 1)[1].strip()
    return None


def is_snapshot_version(version: str) -> bool:
    """Check if a version string indicates a snapshot."""
    return 'SNAPSHOT' in version.upper()


def find_built_jars(goss_dir: Path) -> list[Path]:
    """Find all built JAR files in GOSS project."""
    jars = []

    # Look in generated directories for bundle JARs
    for generated_dir in goss_dir.rglob('generated'):
        for jar in generated_dir.glob('*.jar'):
            if jar.is_file():
                jars.append(jar)

    return jars


def get_bundle_name_from_jar(jar_path: Path) -> str | None:
    """Extract Bundle-SymbolicName from JAR manifest."""
    try:
        with zipfile.ZipFile(jar_path, 'r') as zf:
            manifest_data = zf.read('META-INF/MANIFEST.MF').decode('utf-8')
    except (zipfile.BadZipFile, KeyError, IOError):
        return None

    # Parse manifest
    lines = manifest_data.replace('\r\n ', '').replace('\r\n\t', '').split('\r\n')
    if len(lines) == 1:
        lines = manifest_data.replace('\n ', '').replace('\n\t', '').split('\n')

    for line in lines:
        if line.startswith('Bundle-SymbolicName:'):
            bsn = line.split(':', 1)[1].strip()
            # Remove directives
            if ';' in bsn:
                bsn = bsn.split(';')[0].strip()
            return bsn
    return None


def main() -> int:
    parser = argparse.ArgumentParser(
        description='Push GOSS artifacts to GOSS-Repository (release or snapshot)',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog='''
Examples:
  %(prog)s --snapshot              # Push snapshot versions to snapshot/
  %(prog)s --release               # Push release versions to release/
  %(prog)s --snapshot --dry-run    # Show what would be copied
  %(prog)s --repo /path/to/GOSS-Repository --snapshot
'''
    )
    parser.add_argument(
        '--repo', '-r',
        type=Path,
        default=None,
        help='Path to GOSS-Repository (default: ../GOSS-Repository)'
    )
    target_group = parser.add_mutually_exclusive_group(required=True)
    target_group.add_argument(
        '--snapshot', '-s',
        action='store_true',
        help='Push to snapshot/ directory'
    )
    target_group.add_argument(
        '--release',
        action='store_true',
        help='Push to release/ directory'
    )
    parser.add_argument(
        '--dry-run', '-n',
        action='store_true',
        help='Show what would be copied without actually copying'
    )
    parser.add_argument(
        '--no-index',
        action='store_true',
        help='Skip generating repository index after copying'
    )
    parser.add_argument(
        '--force', '-f',
        action='store_true',
        help='Overwrite existing JARs even if same size'
    )

    args = parser.parse_args()

    script_dir = Path(__file__).parent.resolve()

    # Determine repository path
    if args.repo:
        goss_repo_dir = args.repo.resolve()
    else:
        goss_repo_dir = script_dir.parent / 'GOSS-Repository'

    # Determine target directory
    if args.snapshot:
        target_name = 'snapshot'
    else:
        target_name = 'release'

    dest_repo_dir = goss_repo_dir / target_name

    # Validate destination repository
    if not goss_repo_dir.is_dir():
        log_error(f"GOSS-Repository not found at: {goss_repo_dir}")
        print()
        print(f"  The GOSS-Repository must be cloned locally as a sibling directory.")
        print(f"  Expected location: {goss_repo_dir}")
        print()
        print(f"  To fix this, clone the repository:")
        print(f"    cd {script_dir.parent}")
        print(f"    git clone https://github.com/GridOPTICS/GOSS-Repository.git")
        print()
        print(f"  Or specify a custom path with --repo:")
        print(f"    {sys.argv[0]} --repo /path/to/GOSS-Repository --{target_name}")
        return 1

    if not dest_repo_dir.is_dir():
        log_error(f"Target directory not found: {dest_repo_dir}")
        print()
        print(f"  The '{target_name}/' directory does not exist in GOSS-Repository.")
        print(f"  Please create it or check that you have the correct repository.")
        return 1

    log_info(f"GOSS Directory: {script_dir}")
    log_info(f"Target: {dest_repo_dir}")

    if args.dry_run:
        log_info(f"{Colors.YELLOW}DRY RUN - no files will be copied{Colors.NC}")

    # Find built JARs
    built_jars = find_built_jars(script_dir)

    if not built_jars:
        log_warn("No built JARs found. Run './gradlew build' first.")
        return 1

    log_info(f"Found {len(built_jars)} built JAR(s)")

    # Track statistics
    copied_count = 0
    skipped_count = 0
    updated_count = 0
    version_mismatch_count = 0

    # Process each JAR
    for jar_file in sorted(built_jars):
        version = extract_bundle_version(jar_file)
        bsn = get_bundle_name_from_jar(jar_file)

        if not version or not bsn:
            log_warn(f"  Skipping (no OSGi metadata): {jar_file.name}")
            continue

        # Check if version matches target type
        is_snapshot = is_snapshot_version(version)
        if args.snapshot and not is_snapshot:
            version_mismatch_count += 1
            continue
        if args.release and is_snapshot:
            version_mismatch_count += 1
            continue

        # Determine destination path: <target>/<bsn>/<bsn>-<version>.jar
        dest_dir = dest_repo_dir / bsn
        dest_filename = f"{bsn}-{version}.jar"
        dest_path = dest_dir / dest_filename

        # Check if already exists
        if dest_path.exists():
            source_size = jar_file.stat().st_size
            dest_size = dest_path.stat().st_size

            if source_size == dest_size and not args.force:
                skipped_count += 1
                continue
            else:
                if not args.dry_run:
                    shutil.copy2(str(jar_file), str(dest_path))
                log_info(f"  Updated: {bsn}/{dest_filename}")
                updated_count += 1
        else:
            if not args.dry_run:
                dest_dir.mkdir(parents=True, exist_ok=True)
                shutil.copy2(str(jar_file), str(dest_path))
            log_info(f"  Copied: {bsn}/{dest_filename}")
            copied_count += 1

    # Summary
    print()
    print(f"{Colors.GREEN}========================================{Colors.NC}")
    print(f"{Colors.GREEN}Push to GOSS-Repository Complete!{Colors.NC}")
    print(f"  Target:             {Colors.CYAN}{target_name}/{Colors.NC}")
    print(f"  New JARs copied:    {Colors.GREEN}{copied_count}{Colors.NC}")
    print(f"  JARs updated:       {Colors.BLUE}{updated_count}{Colors.NC}")
    print(f"  JARs skipped:       {Colors.YELLOW}{skipped_count}{Colors.NC} (same size, use --force to overwrite)")
    if version_mismatch_count > 0:
        print(f"  Version mismatch:   {Colors.YELLOW}{version_mismatch_count}{Colors.NC} (wrong type for target)")
    print(f"{Colors.GREEN}========================================{Colors.NC}")
    print()

    # Generate repository index
    if not args.no_index and not args.dry_run and (copied_count > 0 or updated_count > 0):
        log_info(f"Generating repository index for {target_name}/...")

        sh_script = goss_repo_dir / 'generate-repository-index.sh'

        if sh_script.exists():
            result = subprocess.run(
                ['bash', str(sh_script), target_name],
                cwd=goss_repo_dir
            )
            if result.returncode != 0:
                log_warn("generate-repository-index.sh failed")
        else:
            log_warn("generate-repository-index.sh not found, skipping index generation")

    if args.dry_run:
        log_info(f"{Colors.YELLOW}DRY RUN complete - no files were modified{Colors.NC}")
    else:
        log_info(f"{Colors.GREEN}✓ All done!{Colors.NC}")

    return 0


if __name__ == '__main__':
    sys.exit(main())
