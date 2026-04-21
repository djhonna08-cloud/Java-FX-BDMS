# PatternConfig.psm1
# Pattern configuration loading and validation

<#
.SYNOPSIS
    Loads pattern configuration from JSON file.

.PARAMETER ConfigPath
    Path to the pattern configuration JSON file.

.OUTPUTS
    Returns array of pattern objects or falls back to built-in defaults on error.
#>
function Load-PatternConfig {
    param(
        [Parameter(Mandatory=$true)]
        [string]$ConfigPath
    )
    
    $patterns = @()
    
    # Check if file exists
    if (-not (Test-Path $ConfigPath)) {
        Write-Warning "Pattern configuration file not found: $ConfigPath"
        Write-Warning "Falling back to built-in default patterns"
        return Get-DefaultPatterns
    }
    
    try {
        # Read and parse JSON
        $content = Get-Content $ConfigPath -Raw -ErrorAction Stop
        $config = $content | ConvertFrom-Json -ErrorAction Stop
        
        if (-not $config.patterns) {
            Write-Warning "No 'patterns' array found in configuration file"
            return Get-DefaultPatterns
        }
        
        # Validate and process each pattern
        foreach ($pattern in $config.patterns) {
            $validationResult = Test-PatternDefinition -Pattern $pattern
            
            if ($validationResult.IsValid) {
                $patterns += $pattern
            }
            else {
                Write-Warning "Invalid pattern configuration:"
                Write-Warning "  Pattern ID: $($pattern.id)"
                Write-Warning "  Error: $($validationResult.Error)"
                Write-Warning "  This pattern will be skipped"
            }
        }
        
        if ($patterns.Count -eq 0) {
            Write-Warning "No valid patterns found in configuration file"
            Write-Warning "Falling back to built-in default patterns"
            return Get-DefaultPatterns
        }
        
        Write-Verbose "Loaded $($patterns.Count) patterns from $ConfigPath"
        return $patterns
    }
    catch {
        Write-Warning "Failed to load pattern configuration: $_"
        Write-Warning "Falling back to built-in default patterns"
        return Get-DefaultPatterns
    }
}

<#
.SYNOPSIS
    Validates a pattern definition.

.PARAMETER Pattern
    Pattern object to validate.

.OUTPUTS
    Returns hashtable with IsValid and Error properties.
#>
function Test-PatternDefinition {
    param(
        [Parameter(Mandatory=$true)]
        [PSCustomObject]$Pattern
    )
    
    # Check required fields
    $requiredFields = @('id', 'name', 'severity', 'regex')
    
    foreach ($field in $requiredFields) {
        if (-not $Pattern.PSObject.Properties[$field]) {
            return @{
                IsValid = $false
                Error = "Missing required field: $field"
            }
        }
    }
    
    # Validate severity level
    $validSeverities = @('Critical', 'High', 'Medium', 'Low')
    if ($Pattern.severity -notin $validSeverities) {
        return @{
            IsValid = $false
            Error = "Invalid severity level: $($Pattern.severity). Must be one of: $($validSeverities -join ', ')"
        }
    }
    
    # Validate regex pattern
    try {
        $null = [regex]::new($Pattern.regex, [System.Text.RegularExpressions.RegexOptions]::None, [TimeSpan]::FromSeconds(1))
    }
    catch {
        return @{
            IsValid = $false
            Error = "Invalid regex pattern: $_"
        }
    }
    
    # Validate context_check regex if present
    if ($Pattern.PSObject.Properties['context_check'] -and $Pattern.context_check) {
        try {
            $null = [regex]::new($Pattern.context_check, [System.Text.RegularExpressions.RegexOptions]::None, [TimeSpan]::FromSeconds(1))
        }
        catch {
            return @{
                IsValid = $false
                Error = "Invalid context_check regex pattern: $_"
            }
        }
    }
    
    return @{
        IsValid = $true
        Error = $null
    }
}

<#
.SYNOPSIS
    Returns built-in default patterns as fallback.

.OUTPUTS
    Returns array of default pattern objects.
#>
function Get-DefaultPatterns {
    return @(
        @{
            id = "DEFAULT-001"
            name = "Database Call Without Thread Safety"
            category = "Performance"
            severity = "Critical"
            description = "Database method call without proper thread handling"
            regex = "DatabaseHelper\.[a-zA-Z]+\("
            context_check = "Platform\.runLater|new Task<|new Service<"
            multiline = $false
            context_lines = 5
            fix_suggestion = "Wrap database calls in Platform.runLater() or use background threads"
            example_correct = "Platform.runLater(() -> { DatabaseHelper.getData(); });"
        },
        @{
            id = "DEFAULT-002"
            name = "SQL Injection Risk"
            category = "Security"
            severity = "Critical"
            description = "SQL query with string concatenation"
            regex = "String\s+\w+\s*=\s*`"(SELECT|INSERT|UPDATE|DELETE).*?`"\s*\+"
            multiline = $false
            context_lines = 5
            fix_suggestion = "Use PreparedStatement with parameterized queries"
            example_correct = "PreparedStatement pstmt = conn.prepareStatement(`"SELECT * FROM users WHERE id = ?`");"
        }
    )
}

<#
.SYNOPSIS
    Gets pattern by ID from pattern array.

.PARAMETER Patterns
    Array of pattern objects.

.PARAMETER PatternID
    Pattern ID to search for.

.OUTPUTS
    Returns pattern object or $null if not found.
#>
function Get-PatternByID {
    param(
        [Parameter(Mandatory=$true)]
        [array]$Patterns,
        
        [Parameter(Mandatory=$true)]
        [string]$PatternID
    )
    
    return $Patterns | Where-Object { $_.id -eq $PatternID } | Select-Object -First 1
}

<#
.SYNOPSIS
    Groups patterns by category.

.PARAMETER Patterns
    Array of pattern objects.

.OUTPUTS
    Returns hashtable with categories as keys and pattern arrays as values.
#>
function Group-PatternsByCategory {
    param(
        [Parameter(Mandatory=$true)]
        [array]$Patterns
    )
    
    $grouped = @{}
    
    foreach ($pattern in $Patterns) {
        $category = if ($pattern.category) { $pattern.category } else { "Uncategorized" }
        
        if (-not $grouped.ContainsKey($category)) {
            $grouped[$category] = @()
        }
        
        $grouped[$category] += $pattern
    }
    
    return $grouped
}

<#
.SYNOPSIS
    Groups patterns by severity.

.PARAMETER Patterns
    Array of pattern objects.

.OUTPUTS
    Returns hashtable with severity levels as keys and pattern arrays as values.
#>
function Group-PatternsBySeverity {
    param(
        [Parameter(Mandatory=$true)]
        [array]$Patterns
    )
    
    $grouped = @{
        'Critical' = @()
        'High' = @()
        'Medium' = @()
        'Low' = @()
    }
    
    foreach ($pattern in $Patterns) {
        $severity = $pattern.severity
        if ($grouped.ContainsKey($severity)) {
            $grouped[$severity] += $pattern
        }
    }
    
    return $grouped
}

# Export functions
Export-ModuleMember -Function @(
    'Load-PatternConfig',
    'Test-PatternDefinition',
    'Get-DefaultPatterns',
    'Get-PatternByID',
    'Group-PatternsByCategory',
    'Group-PatternsBySeverity'
)
