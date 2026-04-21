# cleanup-project.ps1
# Project Structure Cleanup Script for Barangay San Marino BDMS
# Run this script from the project root directory

Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Barangay San Marino BDMS - Project Cleanup      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Create new directories
Write-Host "[1/5] Creating directory structure..." -ForegroundColor Yellow
$directories = @("docs", "scripts", "tools", "data")
foreach ($dir in $directories) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
        Write-Host "  ✓ Created $dir/" -ForegroundColor Green
    } else {
        Write-Host "  ℹ Directory $dir/ already exists" -ForegroundColor Gray
    }
}
Write-Host ""

# Delete duplicate and unnecessary files
Write-Host "[2/5] Deleting unnecessary files..." -ForegroundColor Yellow
$filesToDelete = @(
    "dark-theme.css",
    "light-theme.css",
    "ExportDatabase.class"
)

foreach ($file in $filesToDelete) {
    if (Test-Path $file) {
        Remove-Item -Path $file -Force
        Write-Host "  ✓ Deleted $file" -ForegroundColor Green
    } else {
        Write-Host "  ℹ File $file not found (already deleted?)" -ForegroundColor Gray
    }
}
Write-Host ""

# Move documentation files
Write-Host "[3/5] Moving documentation files..." -ForegroundColor Yellow
$docFiles = @(
    "DATABASE_SCHEMA.md",
    "INTEGRATION_READINESS_REPORT.md",
    "PRODUCTION_EVALUATION.md",
    "TESTING_LOG.md",
    "DATA_CONSISTENCY_FIXES.md",
    "DASHBOARD_WIDTH_FIX.md",
    "MANUAL_TEST_CHECKLIST.md"
)

foreach ($file in $docFiles) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/" -Force
        Write-Host "  ✓ Moved $file to docs/" -ForegroundColor Green
    } else {
        Write-Host "  ℹ File $file not found" -ForegroundColor Gray
    }
}
Write-Host ""

# Move script files
Write-Host "[4/5] Moving script files..." -ForegroundColor Yellow
$scriptFiles = @(
    "run.bat",
    "run-quick.bat",
    "fix-vscode-java.ps1",
    "open-vscode.ps1"
)

foreach ($file in $scriptFiles) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "scripts/" -Force
        Write-Host "  ✓ Moved $file to scripts/" -ForegroundColor Green
    } else {
        Write-Host "  ℹ File $file not found" -ForegroundColor Gray
    }
}
Write-Host ""

# Move tool and data files
Write-Host "[5/5] Moving tool and data files..." -ForegroundColor Yellow

# Tool files
$toolFiles = @(
    "ExportDatabase.java",
    "StartH2Console.java"
)

foreach ($file in $toolFiles) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "tools/" -Force
        Write-Host "  ✓ Moved $file to tools/" -ForegroundColor Green
    } else {
        Write-Host "  ℹ File $file not found" -ForegroundColor Gray
    }
}

# Data files
$dataFiles = @(
    "sample_residents_import.csv",
    "bdms_dump.sql"
)

foreach ($file in $dataFiles) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "data/" -Force
        Write-Host "  ✓ Moved $file to data/" -ForegroundColor Green
    } else {
        Write-Host "  ℹ File $file not found" -ForegroundColor Gray
    }
}
Write-Host ""

# Summary
Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║          Project Cleanup Complete! ✓               ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "📁 New Directory Structure:" -ForegroundColor Cyan
Write-Host "  • docs/       - All documentation files" -ForegroundColor White
Write-Host "  • scripts/    - Batch and PowerShell scripts" -ForegroundColor White
Write-Host "  • tools/      - Development utility tools" -ForegroundColor White
Write-Host "  • data/       - Sample data and database dumps" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  Important Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Update script paths in your shortcuts/IDE" -ForegroundColor White
Write-Host "     - Old: .\run-quick.bat" -ForegroundColor Gray
Write-Host "     - New: .\scripts\run-quick.bat" -ForegroundColor Green
Write-Host ""
Write-Host "  2. Review changes with: git status" -ForegroundColor White
Write-Host ""
Write-Host "  3. Test the application to ensure everything works" -ForegroundColor White
Write-Host ""
Write-Host "  4. Commit the changes:" -ForegroundColor White
Write-Host "     git add ." -ForegroundColor Gray
Write-Host "     git commit -m 'Reorganize project structure'" -ForegroundColor Gray
Write-Host ""
Write-Host "📖 For more details, see: .kiro/steering/project-structure.md" -ForegroundColor Cyan
Write-Host ""
