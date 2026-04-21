# PesterConfiguration.ps1
# Configuration for Pester test framework

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
else {
    Write-Host "Pester version $($pesterModule.Version) is installed." -ForegroundColor Green
}

# Import Pester module
Import-Module Pester -ErrorAction Stop

# Create Pester configuration
$pesterConfig = New-PesterConfiguration
$pesterConfig.Run.Path = "$PSScriptRoot"
$pesterConfig.Run.PassThru = $true
$pesterConfig.Output.Verbosity = 'Detailed'
$pesterConfig.TestResult.Enabled = $true
$pesterConfig.TestResult.OutputPath = "$PSScriptRoot/../test-results/TestResults.xml"
$pesterConfig.TestResult.OutputFormat = 'NUnitXml'
$pesterConfig.CodeCoverage.Enabled = $false

Write-Host "Pester configuration created." -ForegroundColor Green
Write-Host "Test path: $($pesterConfig.Run.Path)" -ForegroundColor Cyan
Write-Host "Output path: $($pesterConfig.TestResult.OutputPath)" -ForegroundColor Cyan

# Export configuration
$global:ScannerPesterConfig = $pesterConfig
