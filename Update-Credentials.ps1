# Update User Credentials Script
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Updating User Credentials in Database" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find H2 JAR in Maven repository
$h2Jar = "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.2.220\h2-2.2.220.jar"

if (-not (Test-Path $h2Jar)) {
    Write-Host "Error: H2 database JAR not found at $h2Jar" -ForegroundColor Red
    Write-Host "Please run the application first to download dependencies." -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "Compiling UpdateCredentials.java..." -ForegroundColor Yellow
javac -cp "$h2Jar" UpdateCredentials.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "Running credential updater..." -ForegroundColor Yellow
Write-Host ""
java -cp ".;$h2Jar" UpdateCredentials

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Credentials Updated Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "You can now login with the new credentials." -ForegroundColor White
Write-Host "See LOGIN_CREDENTIALS.md for full list." -ForegroundColor White
Write-Host ""

# Clean up compiled class
Remove-Item UpdateCredentials.class -ErrorAction SilentlyContinue

pause
