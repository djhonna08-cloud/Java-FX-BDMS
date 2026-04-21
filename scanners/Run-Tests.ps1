# Run-Tests.ps1
# Test runner script for scanner unit and integration tests

param(
    [Parameter(Mandatory=$false)]
    [string]$TestPath = "$PSScriptRoot/tests",
    
    [Parameter(Mandatory=$false)]
    [switch]$Coverage
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Scanner Test Runner" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Ensure test results directory exists
$testResultsDir = "$PSScriptRoot/test-results"
if (-not (Test-Path $testResultsDir)) {
    New-Item -ItemType Directory -Path $testResultsDir -Force | Out-Null
}

# Check if Pester is installed
$pesterModule = Get-Module -ListAvailable -Name Pester

if (-not $pesterModule) {
    Write-Host "Pester module not found. Installing Pester..." -ForegroundColor Yellow
    try {
        Install-Module -Name Pester -Force -SkipPublisherCheck -Scope CurrentUser
        Write-Host "Pester installed successfully." -ForegroundColor Green
    }
    catch {
        Write-Error "Failed to install Pester: $_"
        Write-Host "Please install Pester manually: Install-Module -Name Pester -Force" -ForegroundColor Red
        exit 1
    }
}

# Import Pester
Import-Module Pester -ErrorAction Stop

# Create Pester configuration
$pesterConfig = New-PesterConfiguration
$pesterConfig.Run.Path = $TestPath
$pesterConfig.Run.PassThru = $true
$pesterConfig.Output.Verbosity = 'Detailed'
$pesterConfig.TestResult.Enabled = $true
$pesterConfig.TestResult.OutputPath = "$testResultsDir/TestResults.xml"
$pesterConfig.TestResult.OutputFormat = 'NUnitXml'

if ($Coverage) {
    $pesterConfig.CodeCoverage.Enabled = $true
    $pesterConfig.CodeCoverage.Path = @(
        "$PSScriptRoot/modules/*.psm1",
        "$PSScriptRoot/*.ps1"
    )
    $pesterConfig.CodeCoverage.OutputPath = "$testResultsDir/coverage.xml"
    $pesterConfig.CodeCoverage.OutputFormat = 'JaCoCo'
}

Write-Host "Running tests from: $TestPath" -ForegroundColor Cyan
Write-Host ""

# Run tests
$result = Invoke-Pester -Configuration $pesterConfig

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Tests: $($result.TotalCount)" -ForegroundColor White
Write-Host "Passed: $($result.PassedCount)" -ForegroundColor Green
Write-Host "Failed: $($result.FailedCount)" -ForegroundColor $(if ($result.FailedCount -gt 0) { 'Red' } else { 'Green' })
Write-Host "Skipped: $($result.SkippedCount)" -ForegroundColor Yellow
Write-Host ""

if ($result.FailedCount -gt 0) {
    Write-Host "TESTS FAILED" -ForegroundColor Red
    exit 1
}
else {
    Write-Host "ALL TESTS PASSED" -ForegroundColor Green
    exit 0
}
