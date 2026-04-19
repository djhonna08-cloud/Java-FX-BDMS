# PowerShell script to open VS Code with proper Java configuration
Write-Host "Opening VS Code with Java configuration..." -ForegroundColor Green
Write-Host ""

# Set Java home environment variable for this session
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify Java
Write-Host "Java Version:" -ForegroundColor Yellow
java -version
Write-Host ""

# Open VS Code in current directory
Write-Host "Starting VS Code..." -ForegroundColor Green
code .

Write-Host ""
Write-Host "VS Code is starting..." -ForegroundColor Cyan
Write-Host ""
Write-Host "IMPORTANT: Wait for Java Language Server to load!" -ForegroundColor Yellow
Write-Host "Look at the bottom-right corner for Java icon and 'Ready' status" -ForegroundColor Yellow
Write-Host ""
Write-Host "Then press F5 to run the application" -ForegroundColor Green
