# FXMLParser.psm1
# FXML file parsing functions

<#
.SYNOPSIS
    Parses an FXML file and extracts UI elements with fx:id attributes.

.PARAMETER FilePath
    Path to the FXML file.

.OUTPUTS
    Returns array of hashtables with ID, Type, File, and Line properties.
#>
function Parse-FXMLFile {
    param(
        [Parameter(Mandatory=$true)]
        [string]$FilePath
    )
    
    if (-not (Test-Path $FilePath)) {
        throw "FXML file not found: $FilePath"
    }
    
    $elements = @()
    
    try {
        # Read file content
        $content = Get-Content $FilePath -Raw
        
        # Parse as XML
        [xml]$xml = $content
        
        # Get all elements with fx:id attribute
        $allNodes = $xml.SelectNodes("//*[@fx:id]", $null)
        
        # If namespace-aware selection fails, try without namespace
        if ($null -eq $allNodes -or $allNodes.Count -eq 0) {
            # Try alternative approach: search for fx:id in raw content
            $lines = Get-Content $FilePath
            for ($i = 0; $i -lt $lines.Count; $i++) {
                $line = $lines[$i]
                
                # Match fx:id="value" and extract element type
                if ($line -match '<(\w+)[^>]*fx:id="([^"]+)"') {
                    $elementType = $matches[1]
                    $fxId = $matches[2]
                    
                    $elements += @{
                        ID = $fxId
                        Type = $elementType
                        File = Split-Path $FilePath -Leaf
                        FilePath = $FilePath
                        Line = $i + 1
                    }
                }
            }
        }
        else {
            # Process nodes found via XPath
            foreach ($node in $allNodes) {
                $fxId = $node.GetAttribute("fx:id")
                $elementType = $node.LocalName
                
                # Try to find line number (approximate)
                $lineNumber = 1
                $lines = Get-Content $FilePath
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    if ($lines[$i] -match "fx:id=`"$fxId`"") {
                        $lineNumber = $i + 1
                        break
                    }
                }
                
                $elements += @{
                    ID = $fxId
                    Type = $elementType
                    File = Split-Path $FilePath -Leaf
                    FilePath = $FilePath
                    Line = $lineNumber
                }
            }
        }
    }
    catch {
        Write-Warning "Failed to parse FXML file as XML: $FilePath - $_"
        Write-Warning "Attempting fallback regex parsing..."
        
        # Fallback: regex-based parsing
        $lines = Get-Content $FilePath
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            
            if ($line -match '<(\w+)[^>]*fx:id="([^"]+)"') {
                $elementType = $matches[1]
                $fxId = $matches[2]
                
                $elements += @{
                    ID = $fxId
                    Type = $elementType
                    File = Split-Path $FilePath -Leaf
                    FilePath = $FilePath
                    Line = $i + 1
                }
            }
        }
    }
    
    return $elements
}

<#
.SYNOPSIS
    Parses all FXML files in a directory.

.PARAMETER DirectoryPath
    Path to directory containing FXML files.

.PARAMETER Recursive
    Whether to search subdirectories recursively.

.OUTPUTS
    Returns array of all FXML elements from all files.
#>
function Parse-FXMLDirectory {
    param(
        [Parameter(Mandatory=$true)]
        [string]$DirectoryPath,
        
        [Parameter(Mandatory=$false)]
        [switch]$Recursive
    )
    
    if (-not (Test-Path $DirectoryPath)) {
        throw "Directory not found: $DirectoryPath"
    }
    
    $allElements = @()
    
    # Find all FXML files
    $fxmlFiles = if ($Recursive) {
        Get-ChildItem -Path $DirectoryPath -Filter "*.fxml" -Recurse -File
    }
    else {
        Get-ChildItem -Path $DirectoryPath -Filter "*.fxml" -File
    }
    
    foreach ($file in $fxmlFiles) {
        try {
            $elements = Parse-FXMLFile -FilePath $file.FullName
            $allElements += $elements
        }
        catch {
            Write-Warning "Failed to parse FXML file: $($file.FullName) - $_"
        }
    }
    
    return $allElements
}

<#
.SYNOPSIS
    Finds FXML element by fx:id.

.PARAMETER Elements
    Array of FXML elements from Parse-FXMLFile or Parse-FXMLDirectory.

.PARAMETER ID
    The fx:id to search for.

.OUTPUTS
    Returns matching element or $null if not found.
#>
function Find-FXMLElement {
    param(
        [Parameter(Mandatory=$true)]
        [array]$Elements,
        
        [Parameter(Mandatory=$true)]
        [string]$ID
    )
    
    return $Elements | Where-Object { $_.ID -eq $ID } | Select-Object -First 1
}

<#
.SYNOPSIS
    Gets all unique fx:id values from FXML elements.

.PARAMETER Elements
    Array of FXML elements.

.OUTPUTS
    Returns array of unique fx:id strings.
#>
function Get-FXMLIDs {
    param(
        [Parameter(Mandatory=$true)]
        [array]$Elements
    )
    
    return $Elements | Select-Object -ExpandProperty ID -Unique
}

<#
.SYNOPSIS
    Groups FXML elements by type.

.PARAMETER Elements
    Array of FXML elements.

.OUTPUTS
    Returns hashtable with element types as keys and arrays of elements as values.
#>
function Group-FXMLElementsByType {
    param(
        [Parameter(Mandatory=$true)]
        [array]$Elements
    )
    
    $grouped = @{}
    
    foreach ($element in $Elements) {
        $type = $element.Type
        if (-not $grouped.ContainsKey($type)) {
            $grouped[$type] = @()
        }
        $grouped[$type] += $element
    }
    
    return $grouped
}

# Export functions
Export-ModuleMember -Function @(
    'Parse-FXMLFile',
    'Parse-FXMLDirectory',
    'Find-FXMLElement',
    'Get-FXMLIDs',
    'Group-FXMLElementsByType'
)
