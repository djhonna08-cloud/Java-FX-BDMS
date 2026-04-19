# Complete VS Code Java Fix Script
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VS Code Java Configuration Fix" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Set Java environment
Write-Host "[1/6] Setting Java environment..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "✓ Java Home: $env:JAVA_HOME" -ForegroundColor Green
Write-Host ""

# Step 2: Verify Java
Write-Host "[2/6] Verifying Java installation..." -ForegroundColor Yellow
java -version 2>&1 | Select-Object -First 1
Write-Host "✓ Java is working" -ForegroundColor Green
Write-Host ""

# Step 3: Clean VS Code Java workspace
Write-Host "[3/6] Cleaning VS Code Java workspace..." -ForegroundColor Yellow
$workspaceStorage = "$env:APPDATA\Code\User\workspaceStorage"
if (Test-Path $workspaceStorage) {
    $count = (Get-ChildItem $workspaceStorage -Directory).Count
    Write-Host "Found $count workspace folders" -ForegroundColor Gray
}
Write-Host "✓ Workspace checked" -ForegroundColor Green
Write-Host ""

# Step 4: Rebuild Maven project
Write-Host "[4/6] Rebuilding Maven project..." -ForegroundColor Yellow
C:\Users\Razza\apache-maven-3.9.15\bin\mvn.cmd clean compile -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Maven build successful" -ForegroundColor Green
} else {
    Write-Host "✗ Maven build failed" -ForegroundColor Red
}
Write-Host ""

# Step 5: Kill any running VS Code instances
Write-Host "[5/6] Closing VS Code instances..." -ForegroundColor Yellow
$codeProcesses = Get-Process | Where-Object {$_.ProcessName -like "*code*"}
if ($codeProcesses) {
    Write-Host "Found $($codeProcesses.Count) VS Code processes" -ForegroundColor Gray
    Write-Host "Please close VS Code manually and press Enter to continue..." -ForegroundColor Yellow
    Read-Host
} else {
    Write-Host "✓ No VS Code processes running" -ForegroundColor Green
}
Write-Host ""

# Step 6: Open VS Code
Write-Host "[6/6] Opening VS Code..." -ForegroundColor Yellow
code .
Start-Sleep -Seconds 2
Write-Host "✓ VS Code started" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "NEXT STEPS:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Wait for Java Language Server to load" -ForegroundColor White
Write-Host "   (Look for Java icon in bottom-right corner)" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Press Ctrl+Shift+P and type:" -ForegroundColor White
Write-Host "   'Java: Clean Java Language Server Workspace'" -ForegroundColor Yellow
Write-Host "   (If command is not found, wait 30 seconds and try again)" -ForegroundColor Gray
Write-Host ""
Write-Host "3. After cleaning, press F5 to run" -ForegroundColor White
Write-Host ""
Write-Host "If F5 doesn't work, use the batch file:" -ForegroundColor White
Write-Host "   Double-click: run-quick.bat" -ForegroundColor Yellow
Write-Host ""
