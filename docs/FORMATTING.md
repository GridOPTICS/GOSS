# Code Formatting Guide

This project uses consistent code formatting across Eclipse and VSCode editors, enforced by Spotless.

## Formatting Rules

- **Indentation**: 4 spaces (not tabs)
- **Line length**: 120 characters
- **Braces**: End of line (K&R style)
- **File endings**: Unix line endings (LF), files end with newline
- **Whitespace**: No trailing whitespace

## IDE Configuration

### Eclipse

Eclipse will automatically use the formatter configuration from `.settings/eclipse-java-formatter.xml`.

1. The formatter is automatically imported when you open the project
2. **Format code**: `Ctrl+Shift+F` (Windows/Linux) or `Cmd+Shift+F` (Mac)
3. Eclipse will format according to the GOSS profile

### VSCode

VSCode is configured to use the same Eclipse formatter for consistency.

1. **Format current file**: `Shift+Alt+F` (Windows/Linux) or `Shift+Option+F` (Mac)
2. **Format on save**: Enabled by default (see `.vscode/settings.json`)
3. **Format selection**: Select code, then `Ctrl+K Ctrl+F`

The formatter configuration is in `.vscode/settings.json`:
```json
"java.format.settings.url": ".settings/eclipse-java-formatter.xml"
"java.format.settings.profile": "GOSS"
```

## Gradle Commands

### Check Formatting

Check if code is properly formatted without making changes:

```bash
./gradlew spotlessCheck
```

This will:
- ✅ Pass if all code is properly formatted
- ❌ Fail and show violations if formatting is incorrect

### Apply Formatting

Automatically fix formatting issues:

```bash
./gradlew spotlessApply
```

This will:
- Format all Java files according to the Eclipse formatter
- Remove trailing whitespace
- Ensure files end with newline
- Fix line endings to Unix (LF)

### Format Specific Module

```bash
# Check specific module
./gradlew :pnnl.goss.core:spotlessCheck

# Format specific module
./gradlew :pnnl.goss.core:spotlessApply
```

## CI/CD Integration

### GitHub Actions

A GitHub Actions workflow automatically checks formatting on all pull requests:

**Workflow**: `.github/workflows/format-check.yml`

- Runs on every PR to `master`, `main`, or `develop`
- Uses `./gradlew spotlessCheck` to validate formatting
- ❌ Blocks PR if formatting is incorrect
- 💬 Comments on PR with fix instructions

### Before Committing

**Option 1: Run Spotless manually**
```bash
./gradlew spotlessApply
git add .
git commit -m "Your message"
```

**Option 2: Use IDE formatter**
- Eclipse: `Ctrl+Shift+F`
- VSCode: `Shift+Alt+F` or enable format-on-save

### Pre-commit Hook (Optional)

You can add a pre-commit hook to automatically check formatting:

```bash
# Create .git/hooks/pre-commit
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
./gradlew spotlessCheck
if [ $? -ne 0 ]; then
    echo "❌ Code formatting check failed!"
    echo "Run './gradlew spotlessApply' to fix formatting"
    exit 1
fi
EOF
chmod +x .git/hooks/pre-commit
```

## Troubleshooting

### VSCode formatter not working

1. Reload VSCode Java Language Server:
   - `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace`
2. Verify Java extension is installed:
   - Extension ID: `redhat.java`
3. Check settings point to formatter:
   - Open `.vscode/settings.json`
   - Verify `java.format.settings.url` is set

### Eclipse formatter not applying

1. Verify formatter is imported:
   - Window → Preferences → Java → Code Style → Formatter
   - Should show "GOSS" profile
2. Re-import formatter:
   - Import → `.settings/eclipse-java-formatter.xml`
3. Refresh project:
   - Right-click project → Gradle → Refresh Gradle Project

### Spotless errors after merge

After merging/pulling changes:
```bash
# Apply formatting to all files
./gradlew spotlessApply

# Commit the formatting changes
git add .
git commit -m "Apply code formatting"
```

## Formatting Configuration Files

| File | Purpose |
|------|---------|
| `.settings/eclipse-java-formatter.xml` | Eclipse formatter configuration (canonical) |
| `.settings/org.eclipse.jdt.core.prefs` | Eclipse Java compiler settings |
| `.vscode/settings.json` | VSCode Java formatter settings |
| `build.gradle` | Spotless plugin configuration |
| `.github/workflows/format-check.yml` | CI formatting check |

## Best Practices

1. **Format before committing**: Always run `./gradlew spotlessApply` before pushing
2. **Enable format-on-save**: Both IDEs support automatic formatting
3. **Check CI before merging**: Ensure GitHub Actions passes
4. **Don't mix formatting with logic**: Commit formatting changes separately
5. **Use IDE shortcuts**: Learn keyboard shortcuts for quick formatting

## Questions?

- Check [QUICK-START.md](QUICK-START.md) for getting started
- See [DEVELOPER-SETUP.md](DEVELOPER-SETUP.md) for build and project setup
- Open an issue for formatting configuration questions
