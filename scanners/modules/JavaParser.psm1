# JavaParser.psm1
# Java source code parsing functions

<#
.SYNOPSIS
    Parses Java source code and extracts structural elements.

.PARAMETER FilePath
    Path to the Java source file.

.OUTPUTS
    Returns hashtable with parsed elements: imports, fields, methods, annotations, FXML references.
#>
function Parse-JavaSource {
    param(
        [Parameter(Mandatory=$true)]
        [string]$FilePath
    )
    
    if (-not (Test-Path $FilePath)) {
        throw "File not found: $FilePath"
    }
    
    $lines = Get-Content $FilePath
    $lineCount = $lines.Count
    
    $result = @{
        FilePath = $FilePath
        LineCount = $lineCount
        Imports = @()
        Fields = @()
        Methods = @()
        FXMLReferences = @()
    }
    
    # Parse imports
    for ($i = 0; $i -lt $lineCount; $i++) {
        $line = $lines[$i]
        
        if ($line -match '^\s*import\s+([\w\.]+)\s*;') {
            $result.Imports += @{
                Line = $i + 1
                Package = $matches[1]
            }
        }
    }
    
    # Parse fields with annotations
    for ($i = 0; $i -lt $lineCount; $i++) {
        $line = $lines[$i]
        
        # Check for field declaration (private/public/protected Type fieldName)
        if ($line -match '^\s*(private|public|protected)\s+(static\s+)?(final\s+)?([<>\w\[\]]+)\s+(\w+)\s*[;=]') {
            $fieldType = $matches[4]
            $fieldName = $matches[5]
            
            # Check for annotations on previous lines
            $annotations = @()
            $j = $i - 1
            while ($j -ge 0 -and $lines[$j] -match '^\s*@(\w+)') {
                $annotations += $matches[1]
                $j--
            }
            
            $result.Fields += @{
                Line = $i + 1
                Name = $fieldName
                Type = $fieldType
                Annotations = $annotations
            }
        }
    }
    
    # Parse methods
    for ($i = 0; $i -lt $lineCount; $i++) {
        $line = $lines[$i]
        
        # Match method signatures: visibility [static] [<generics>] returnType methodName(params)
        # This regex handles both regular and generic methods
        if ($line -match '^\s*(private|public|protected)\s+(static\s+)?(<[^>]+>\s+)?([<>\w\[\]]+)\s+(\w+)\s*\(([^\)]*)\)\s*(\{|throws)') {
            $returnType = $matches[4]
            $methodName = $matches[5]
            $parameters = $matches[6]
            
            # Extract method body
            $methodBody = ""
            $braceCount = 0
            $methodStart = $i
            $methodEnd = $i
            
            # Find opening brace
            $j = $i
            while ($j -lt $lineCount) {
                if ($lines[$j] -match '\{') {
                    $braceCount = 1
                    $j++
                    break
                }
                $j++
            }
            
            # Extract body until closing brace
            while ($j -lt $lineCount -and $braceCount -gt 0) {
                $currentLine = $lines[$j]
                $methodBody += $currentLine + "`n"
                
                # Count braces
                $openBraces = ([regex]::Matches($currentLine, '\{')).Count
                $closeBraces = ([regex]::Matches($currentLine, '\}')).Count
                $braceCount += $openBraces - $closeBraces
                
                $methodEnd = $j
                $j++
            }
            
            # Check for annotations
            $annotations = @()
            $k = $i - 1
            while ($k -ge 0 -and $lines[$k] -match '^\s*@(\w+)') {
                $annotations += $matches[1]
                $k--
            }
            
            $result.Methods += @{
                Line = $methodStart + 1
                EndLine = $methodEnd + 1
                Name = $methodName
                ReturnType = $returnType
                Parameters = $parameters
                Body = $methodBody.Trim()
                Annotations = $annotations
            }
        }
    }
    
    # Parse FXML references
    for ($i = 0; $i -lt $lineCount; $i++) {
        $line = $lines[$i]
        
        # Match FXMLLoader.load() with file path
        if ($line -match 'FXMLLoader\.load\([^"]*"([^"]+\.fxml)"') {
            $result.FXMLReferences += @{
                Line = $i + 1
                File = $matches[1]
                LoadMethod = 'FXMLLoader.load()'
            }
        }
        
        # Match getResource() with FXML file
        if ($line -match 'getResource\([^"]*"([^"]+\.fxml)"') {
            $result.FXMLReferences += @{
                Line = $i + 1
                File = $matches[1]
                LoadMethod = 'getResource()'
            }
        }
    }
    
    return $result
}

<#
.SYNOPSIS
    Extracts @FXML annotated fields from parsed source.

.PARAMETER ParsedSource
    Hashtable returned from Parse-JavaSource.

.OUTPUTS
    Returns array of fields with @FXML annotation.
#>
function Get-FXMLFields {
    param(
        [Parameter(Mandatory=$true)]
        [hashtable]$ParsedSource
    )
    
    return $ParsedSource.Fields | Where-Object { $_.Annotations -contains 'FXML' }
}

<#
.SYNOPSIS
    Finds methods matching UI event handler patterns.

.PARAMETER ParsedSource
    Hashtable returned from Parse-JavaSource.

.OUTPUTS
    Returns array of methods that appear to be UI event handlers.
#>
function Get-UIEventHandlers {
    param(
        [Parameter(Mandatory=$true)]
        [hashtable]$ParsedSource
    )
    
    $handlers = @()
    
    foreach ($method in $ParsedSource.Methods) {
        # Check for common event handler naming patterns
        if ($method.Name -match '^(handle|on)[A-Z]' -or 
            $method.Body -match 'setOnAction|setOnMouseClicked|setOnKeyPressed') {
            $handlers += $method
        }
    }
    
    return $handlers
}

<#
.SYNOPSIS
    Searches for database method calls in code.

.PARAMETER CodeBlock
    String containing Java code to search.

.OUTPUTS
    Returns array of hashtables with Line and MethodCall properties.
#>
function Find-DatabaseCalls {
    param(
        [Parameter(Mandatory=$true)]
        [string]$CodeBlock,
        
        [Parameter(Mandatory=$false)]
        [int]$StartLine = 1
    )
    
    $calls = @()
    $lines = $CodeBlock -split "`n"
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        
        # Match DatabaseHelper.methodName( or JDBC operations
        if ($line -match 'DatabaseHelper\.(\w+)\s*\(') {
            $calls += @{
                Line = $StartLine + $i
                MethodCall = $matches[0]
                MethodName = $matches[1]
            }
        }
        
        # Match JDBC operations
        if ($line -match '(Connection|PreparedStatement|ResultSet|Statement)') {
            $calls += @{
                Line = $StartLine + $i
                MethodCall = $matches[0]
                MethodName = $matches[1]
            }
        }
    }
    
    return $calls
}

# Export functions
Export-ModuleMember -Function @(
    'Parse-JavaSource',
    'Get-FXMLFields',
    'Get-UIEventHandlers',
    'Find-DatabaseCalls'
)
