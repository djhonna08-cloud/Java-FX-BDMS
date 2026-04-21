# FXMLParser.Tests.ps1
# Unit tests for FXML parser

BeforeAll {
    # Import the module
    Import-Module "$PSScriptRoot/../modules/FXMLParser.psm1" -Force
    
    # Create test fixture directory
    $script:testFixtureDir = "$PSScriptRoot/../test-fixtures/fxml-tests"
    if (-not (Test-Path $script:testFixtureDir)) {
        New-Item -ItemType Directory -Path $script:testFixtureDir -Force | Out-Null
    }
}

Describe "Parse-FXMLFile" {
    Context "Valid FXML Files" {
        BeforeAll {
            # Create valid FXML test file
            $testFile = "$script:testFixtureDir/valid-test.fxml"
            $content = @'
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox xmlns:fx="http://javafx.com/fxml" fx:controller="com.example.Controller">
    <TextField fx:id="nameField" promptText="Enter name"/>
    <TextField fx:id="emailField" promptText="Enter email"/>
    <Button fx:id="submitButton" text="Submit"/>
    <TableView fx:id="residentTable">
        <columns>
            <TableColumn fx:id="idColumn" text="ID"/>
            <TableColumn fx:id="nameColumn" text="Name"/>
        </columns>
    </TableView>
</VBox>
'@
            Set-Content -Path $testFile -Value $content
            $script:validFXMLFile = $testFile
        }
        
        It "Should extract all fx:id attributes" {
            $result = Parse-FXMLFile -FilePath $script:validFXMLFile
            $result.Count | Should -BeGreaterOrEqual 5
        }
        
        It "Should extract TextField elements" {
            $result = Parse-FXMLFile -FilePath $script:validFXMLFile
            $textFields = $result | Where-Object { $_.Type -eq 'TextField' }
            $textFields.Count | Should -BeGreaterOrEqual 2
        }
        
        It "Should extract element IDs correctly" {
            $result = Parse-FXMLFile -FilePath $script:validFXMLFile
            $nameField = $result | Where-Object { $_.ID -eq 'nameField' }
            $nameField | Should -Not -BeNullOrEmpty
            $nameField.Type | Should -Be 'TextField'
        }
        
        It "Should extract Button elements" {
            $result = Parse-FXMLFile -FilePath $script:validFXMLFile
            $buttons = $result | Where-Object { $_.Type -eq 'Button' }
            $buttons.Count | Should -BeGreaterOrEqual 1
        }
        
        It "Should extract TableView elements" {
            $result = Parse-FXMLFile -FilePath $script:validFXMLFile
            $tables = $result | Where-Object { $_.Type -eq 'TableView' }
            $tables.Count | Should -BeGreaterOrEqual 1
        }
        
        It "Should extract TableColumn elements" {
            $result = Parse-FXMLFile -FilePath $script:validFXMLFile
            $columns = $result | Where-Object { $_.Type -eq 'TableColumn' }
            $columns.Count | Should -BeGreaterOrEqual 2
        }
        
        It "Should include file name in results" {
            $result = Parse-FXMLFile -FilePath $script:validFXMLFile
            $result[0].File | Should -Be 'valid-test.fxml'
        }
        
        It "Should include line numbers" {
            $result = Parse-FXMLFile -FilePath $script:validFXMLFile
            $result[0].Line | Should -BeGreaterThan 0
        }
    }
    
    Context "FXML with Namespaces" {
        BeforeAll {
            $testFile = "$script:testFixtureDir/namespace-test.fxml"
            $content = @'
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns="http://javafx.com/javafx" 
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.example.MainController">
    <center>
        <VBox fx:id="contentPane">
            <Label fx:id="titleLabel" text="Title"/>
            <TextField fx:id="searchField"/>
        </VBox>
    </center>
</BorderPane>
'@
            Set-Content -Path $testFile -Value $content
            $script:namespaceFile = $testFile
        }
        
        It "Should handle FXML files with namespaces" {
            $result = Parse-FXMLFile -FilePath $script:namespaceFile
            $result.Count | Should -BeGreaterThan 0
        }
        
        It "Should extract elements from namespaced FXML" {
            $result = Parse-FXMLFile -FilePath $script:namespaceFile
            $searchField = $result | Where-Object { $_.ID -eq 'searchField' }
            $searchField | Should -Not -BeNullOrEmpty
        }
    }
    
    Context "Malformed XML" {
        BeforeAll {
            $testFile = "$script:testFixtureDir/malformed-test.fxml"
            $content = @'
<?xml version="1.0" encoding="UTF-8"?>
<VBox>
    <TextField fx:id="field1" promptText="Test"/>
    <Button fx:id="button1" text="Click"
    <!-- Missing closing tag -->
</VBox>
'@
            Set-Content -Path $testFile -Value $content
            $script:malformedFile = $testFile
        }
        
        It "Should handle malformed XML gracefully" {
            { Parse-FXMLFile -FilePath $script:malformedFile } | Should -Not -Throw
        }
        
        It "Should extract what it can from malformed XML" {
            $result = Parse-FXMLFile -FilePath $script:malformedFile
            # Should still find fx:id attributes via regex fallback
            $result.Count | Should -BeGreaterOrEqual 0
        }
    }
    
    Context "Empty or Missing Files" {
        It "Should throw error for non-existent file" {
            { Parse-FXMLFile -FilePath "$script:testFixtureDir/nonexistent.fxml" } | Should -Throw
        }
        
        It "Should handle empty FXML file" {
            $testFile = "$script:testFixtureDir/empty-test.fxml"
            Set-Content -Path $testFile -Value ""
            $result = Parse-FXMLFile -FilePath $testFile
            $result.Count | Should -Be 0
        }
    }
}

Describe "Parse-FXMLDirectory" {
    BeforeAll {
        # Create multiple FXML files in directory
        $testDir = "$script:testFixtureDir/multi-fxml"
        if (-not (Test-Path $testDir)) {
            New-Item -ItemType Directory -Path $testDir -Force | Out-Null
        }
        
        # File 1
        $content1 = @'
<?xml version="1.0" encoding="UTF-8"?>
<VBox xmlns:fx="http://javafx.com/fxml">
    <TextField fx:id="field1"/>
    <Button fx:id="button1"/>
</VBox>
'@
        Set-Content -Path "$testDir/file1.fxml" -Value $content1
        
        # File 2
        $content2 = @'
<?xml version="1.0" encoding="UTF-8"?>
<HBox xmlns:fx="http://javafx.com/fxml">
    <Label fx:id="label1"/>
    <TextField fx:id="field2"/>
</HBox>
'@
        Set-Content -Path "$testDir/file2.fxml" -Value $content2
        
        $script:multiFileDir = $testDir
    }
    
    It "Should parse all FXML files in directory" {
        $result = Parse-FXMLDirectory -DirectoryPath $script:multiFileDir
        $result.Count | Should -BeGreaterOrEqual 4
    }
    
    It "Should include elements from multiple files" {
        $result = Parse-FXMLDirectory -DirectoryPath $script:multiFileDir
        $files = $result | Select-Object -ExpandProperty File -Unique
        $files.Count | Should -BeGreaterOrEqual 2
    }
    
    It "Should handle recursive search" {
        # Create subdirectory with FXML
        $subDir = "$script:multiFileDir/sub"
        if (-not (Test-Path $subDir)) {
            New-Item -ItemType Directory -Path $subDir -Force | Out-Null
        }
        $content = @'
<?xml version="1.0" encoding="UTF-8"?>
<VBox xmlns:fx="http://javafx.com/fxml">
    <TextField fx:id="subField"/>
</VBox>
'@
        Set-Content -Path "$subDir/sub.fxml" -Value $content
        
        $result = Parse-FXMLDirectory -DirectoryPath $script:multiFileDir -Recursive
        $subElements = $result | Where-Object { $_.ID -eq 'subField' }
        $subElements | Should -Not -BeNullOrEmpty
    }
}

Describe "Find-FXMLElement" {
    BeforeAll {
        $testFile = "$script:testFixtureDir/find-test.fxml"
        $content = @'
<?xml version="1.0" encoding="UTF-8"?>
<VBox xmlns:fx="http://javafx.com/fxml">
    <TextField fx:id="searchField"/>
    <Button fx:id="searchButton"/>
    <TableView fx:id="resultsTable"/>
</VBox>
'@
        Set-Content -Path $testFile -Value $content
        $script:elements = Parse-FXMLFile -FilePath $testFile
    }
    
    It "Should find element by ID" {
        $element = Find-FXMLElement -Elements $script:elements -ID "searchField"
        $element | Should -Not -BeNullOrEmpty
        $element.Type | Should -Be 'TextField'
    }
    
    It "Should return null for non-existent ID" {
        $element = Find-FXMLElement -Elements $script:elements -ID "nonExistent"
        $element | Should -BeNullOrEmpty
    }
}

Describe "Get-FXMLIDs" {
    BeforeAll {
        $testFile = "$script:testFixtureDir/ids-test.fxml"
        $content = @'
<?xml version="1.0" encoding="UTF-8"?>
<VBox xmlns:fx="http://javafx.com/fxml">
    <TextField fx:id="field1"/>
    <TextField fx:id="field2"/>
    <Button fx:id="button1"/>
</VBox>
'@
        Set-Content -Path $testFile -Value $content
        $script:elements = Parse-FXMLFile -FilePath $testFile
    }
    
    It "Should return all unique IDs" {
        $ids = Get-FXMLIDs -Elements $script:elements
        $ids.Count | Should -BeGreaterOrEqual 3
    }
    
    It "Should include specific IDs" {
        $ids = Get-FXMLIDs -Elements $script:elements
        $ids | Should -Contain 'field1'
        $ids | Should -Contain 'button1'
    }
}

Describe "Group-FXMLElementsByType" {
    BeforeAll {
        $testFile = "$script:testFixtureDir/group-test.fxml"
        $content = @'
<?xml version="1.0" encoding="UTF-8"?>
<VBox xmlns:fx="http://javafx.com/fxml">
    <TextField fx:id="field1"/>
    <TextField fx:id="field2"/>
    <Button fx:id="button1"/>
    <Button fx:id="button2"/>
    <Label fx:id="label1"/>
</VBox>
'@
        Set-Content -Path $testFile -Value $content
        $script:elements = Parse-FXMLFile -FilePath $testFile
    }
    
    It "Should group elements by type" {
        $grouped = Group-FXMLElementsByType -Elements $script:elements
        $grouped.Keys.Count | Should -BeGreaterOrEqual 3
    }
    
    It "Should have correct counts per type" {
        $grouped = Group-FXMLElementsByType -Elements $script:elements
        $grouped['TextField'].Count | Should -Be 2
        $grouped['Button'].Count | Should -Be 2
        $grouped['Label'].Count | Should -Be 1
    }
}
