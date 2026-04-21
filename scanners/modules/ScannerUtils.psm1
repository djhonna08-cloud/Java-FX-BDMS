# ScannerUtils.psm1
# Shared utility functions for frontend and backend scanners

<#
.SYNOPSIS
    Validates that a file exists and is readable.

.PARAMETER FilePath
    Path to the file to validate.

.OUTPUTS
    Returns $true if file exists and is readable, $false otherwise.
#>
function Test-SourceFile {
    param(
        [Parameter(Mandatory=$true)]
        [string]$FilePath
    )
    
    if (-not (Test-Path $FilePath)) {
        Write-Error "File not found: $FilePath"
        return $false
    }
    
    if (-not (Test-Path $FilePath -PathType Leaf)) {
        Write-Error "Path is not a file: $FilePath"
        return $false
    }
    
    try {
        $null = Get-Content $FilePath -TotalCount 1 -ErrorAction Stop
        return $true
    }
    catch {
        Write-Error "File is not readable: $FilePath - $_"
        return $false
    }
}

<#
.SYNOPSIS
    Writes a log message with timestamp and severity level.

.PARAMETER Message
    The message to log.

.PARAMETER Level
    Severity level: Info, Warning, Error, Verbose.

.PARAMETER Verbosity
    Current verbosity setting: quiet, normal, verbose.
#>
function Write-ScannerLog {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Message,
        
        [Parameter(Mandatory=$false)]
        [ValidateSet('Info', 'Warning', 'Error', 'Verbose')]
        [string]$Level = 'Info',
        
        [Parameter(Mandatory=$false)]
        [ValidateSet('quiet', 'normal', 'verbose')]
        [string]$Verbosity = 'normal'
    )
    
    # Quiet mode: only errors
    if ($Verbosity -eq 'quiet' -and $Level -ne 'Error') {
        return
    }
    
    # Normal mode: Info, Warning, Error
    if ($Verbosity -eq 'normal' -and $Level -eq 'Verbose') {
        return
    }
    
    # Verbose mode: all messages
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    
    switch ($Level) {
        'Error' { Write-Host $logMessage -ForegroundColor Red }
        'Warning' { Write-Host $logMessage -ForegroundColor Yellow }
        'Verbose' { Write-Host $logMessage -ForegroundColor Gray }
        default { Write-Host $logMessage }
    }
}

<#
.SYNOPSIS
    Displays a progress indicator for long-running operations.

.PARAMETER Activity
    Description of the activity being performed.

.PARAMETER Status
    Current status message.

.PARAMETER PercentComplete
    Percentage complete (0-100).
#>
function Show-ScannerProgress {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Activity,
        
        [Parameter(Mandatory=$true)]
        [string]$Status,
        
        [Parameter(Mandatory=$false)]
        [int]$PercentComplete = -1
    )
    
    if ($PercentComplete -ge 0) {
        Write-Progress -Activity $Activity -Status $Status -PercentComplete $PercentComplete
    }
    else {
        Write-Progress -Activity $Activity -Status $Status
    }
}

<#
.SYNOPSIS
    Completes and hides the progress indicator.

.PARAMETER Activity
    Description of the activity that was being performed.
#>
function Complete-ScannerProgress {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Activity
    )
    
    Write-Progress -Activity $Activity -Completed
}

<#
.SYNOPSIS
    Validates directory path and creates it if it doesn't exist.

.PARAMETER DirectoryPath
    Path to the directory.

.OUTPUTS
    Returns $true if directory exists or was created successfully.
#>
function Ensure-Directory {
    param(
        [Parameter(Mandatory=$true)]
        [string]$DirectoryPath
    )
    
    if (-not (Test-Path $DirectoryPath)) {
        try {
            New-Item -ItemType Directory -Path $DirectoryPath -Force | Out-Null
            return $true
        }
        catch {
            Write-Error "Failed to create directory: $DirectoryPath - $_"
            return $false
        }
    }
    
    return $true
}

<#
.SYNOPSIS
    Reads file content with error handling.

.PARAMETER FilePath
    Path to the file to read.

.OUTPUTS
    Returns array of lines or $null on error.
#>
function Read-SourceFileContent {
    param(
        [Parameter(Mandatory=$true)]
        [string]$FilePath
    )
    
    try {
        return Get-Content $FilePath -ErrorAction Stop
    }
    catch {
        Write-Error "Failed to read file: $FilePath - $_"
        return $null
    }
}

<#
.SYNOPSIS
    Gets the scanner version from module metadata.

.OUTPUTS
    Returns version string.
#>
function Get-ScannerVersion {
    return "1.0.0"
}

# Export functions
Export-ModuleMember -Function @(
    'Test-SourceFile',
    'Write-ScannerLog',
    'Show-ScannerProgress',
    'Complete-ScannerProgress',
    'Ensure-Directory',
    'Read-SourceFileContent',
    'Get-ScannerVersion'
)
