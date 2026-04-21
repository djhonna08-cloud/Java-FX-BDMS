# JavaParser.Tests.ps1
# Unit tests for Java source parser (Pester 3.x compatible)

# Import the module
Import-Module "$PSScriptRoot/../modules/JavaParser.psm1" -Force

# Create test fixture directory
$script:testFixtureDir = "$PSScriptRoot/../test-fixtures/parser-tests"
if (-not (Test-Path $script:testFixtureDir)) {
    New-Item -ItemType Directory -Path $script:testFixtureDir -Force | Out-Null
}

Describe "Parse-JavaSource" {
    Context "Method Extraction" {
        # Create test file with various method signatures
        $testFile = "$script:testFixtureDir/MethodTest.java"
        $content = @'
package com.example;

public class MethodTest {
    public void publicMethod() {
        System.out.println("public");
    }
    
    private String privateMethod(int param) {
        return "private";
    }
    
    protected static List<String> staticMethod() {
        return new ArrayList<>();
    }
    
    public <T> T genericMethod(T input) {
        return input;
    }
}
'@
        Set-Content -Path $testFile -Value $content
        $script:methodTestFile = $testFile
        
        It "Should extract public methods" {
            $result = Parse-JavaSource -FilePath $script:methodTestFile
            $publicMethods = $result.Methods | Where-Object { $_.Name -eq 'publicMethod' }
            $publicMethods | Should Not BeNullOrEmpty
            $publicMethods[0].ReturnType | Should Be 'void'
        }
        
        It "Should extract private methods with parameters" {
            $result = Parse-JavaSource -FilePath $script:methodTestFile
            $privateMethods = $result.Methods | Where-Object { $_.Name -eq 'privateMethod' }
            $privateMethods | Should Not BeNullOrEmpty
            $privateMethods[0].ReturnType | Should Be 'String'
            $privateMethods[0].Parameters | Should Match 'int param'
        }
        
        It "Should extract static methods" {
            $result = Parse-JavaSource -FilePath $script:methodTestFile
            $staticMethods = $result.Methods | Where-Object { $_.Name -eq 'staticMethod' }
            $staticMethods | Should Not BeNullOrEmpty
        }
        
        It "Should extract generic methods" {
            $result = Parse-JavaSource -FilePath $script:methodTestFile
            $genericMethods = $result.Methods | Where-Object { $_.Name -eq 'genericMethod' }
            $genericMethods | Should Not BeNullOrEmpty
        }
        
        It "Should extract method bodies" {
            $result = Parse-JavaSource -FilePath $script:methodTestFile
            $method = $result.Methods | Where-Object { $_.Name -eq 'publicMethod' } | Select-Object -First 1
            $method.Body | Should Match 'System\.out\.println'
        }
    }
    
    Context "Field Extraction" {
        $testFile = "$script:testFixtureDir/FieldTest.java"
        $content = @'
package com.example;

import javafx.scene.control.TextField;

public class FieldTest {
    @FXML
    private TextField nameField;
    
    @FXML
    @Inject
    private TableView<Resident> residentTable;
    
    private String regularField;
    
    public static final int CONSTANT = 42;
}
'@
        Set-Content -Path $testFile -Value $content
        $script:fieldTestFile = $testFile
        
        It "Should extract fields with @FXML annotation" {
            $result = Parse-JavaSource -FilePath $script:fieldTestFile
            $fxmlFields = $result.Fields | Where-Object { $_.Annotations -contains 'FXML' }
            $fxmlFields.Count | Should BeGreaterThan 0
        }
        
        It "Should extract field names and types" {
            $result = Parse-JavaSource -FilePath $script:fieldTestFile
            $nameField = $result.Fields | Where-Object { $_.Name -eq 'nameField' }
            $nameField | Should Not BeNullOrEmpty
            $nameField.Type | Should Be 'TextField'
        }
        
        It "Should extract multiple annotations" {
            $result = Parse-JavaSource -FilePath $script:fieldTestFile
            $tableField = $result.Fields | Where-Object { $_.Name -eq 'residentTable' }
            $tableField.Annotations -contains 'FXML' | Should Be $true
            $tableField.Annotations -contains 'Inject' | Should Be $true
        }
        
        It "Should extract fields without annotations" {
            $result = Parse-JavaSource -FilePath $script:fieldTestFile
            $regularField = $result.Fields | Where-Object { $_.Name -eq 'regularField' }
            $regularField | Should Not BeNullOrEmpty
            $regularField.Annotations.Count | Should Be 0
        }
    }
    
    Context "Import Parsing" {
        $testFile = "$script:testFixtureDir/ImportTest.java"
        $content = @'
package com.example;

import javafx.application.Platform;
import javafx.scene.control.*;
import com.example.DatabaseHelper;
import java.util.List;

public class ImportTest {
}
'@
        Set-Content -Path $testFile -Value $content
        $script:importTestFile = $testFile
        
        It "Should extract all imports" {
            $result = Parse-JavaSource -FilePath $script:importTestFile
            $result.Imports.Count | Should BeGreaterThan 0
        }
        
        It "Should extract specific import packages" {
            $result = Parse-JavaSource -FilePath $script:importTestFile
            $platformImport = $result.Imports | Where-Object { $_.Package -eq 'javafx.application.Platform' }
            $platformImport | Should Not BeNullOrEmpty
        }
    }
    
    Context "FXML Reference Detection" {
        $testFile = "$script:testFixtureDir/FXMLRefTest.java"
        $content = @'
package com.example;

public class FXMLRefTest {
    public void loadView() {
        FXMLLoader.load(getClass().getResource("main-view.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/views/dialog.fxml"));
    }
}
'@
        Set-Content -Path $testFile -Value $content
        $script:fxmlRefTestFile = $testFile
        
        It "Should detect FXML file references" {
            $result = Parse-JavaSource -FilePath $script:fxmlRefTestFile
            $result.FXMLReferences.Count | Should BeGreaterThan 0
        }
        
        It "Should extract FXML file names" {
            $result = Parse-JavaSource -FilePath $script:fxmlRefTestFile
            $mainView = $result.FXMLReferences | Where-Object { $_.File -eq 'main-view.fxml' }
            $mainView | Should Not BeNullOrEmpty
        }
    }
    
    Context "Edge Cases" {
        It "Should handle nested classes" {
            $testFile = "$script:testFixtureDir/NestedClassTest.java"
            $content = @'
public class Outer {
    public void outerMethod() {
        System.out.println("outer");
    }
    
    class Inner {
        public void innerMethod() {
            System.out.println("inner");
        }
    }
}
'@
            Set-Content -Path $testFile -Value $content
            $result = Parse-JavaSource -FilePath $testFile
            $result.Methods.Count | Should BeGreaterThan 0
        }
        
        It "Should handle lambda expressions" {
            $testFile = "$script:testFixtureDir/LambdaTest.java"
            $content = @'
public class LambdaTest {
    public void setupHandlers() {
        button.setOnAction(e -> handleClick());
        list.forEach(item -> System.out.println(item));
    }
}
'@
            Set-Content -Path $testFile -Value $content
            $result = Parse-JavaSource -FilePath $testFile
            $result.Methods | Where-Object { $_.Name -eq 'setupHandlers' } | Should Not BeNullOrEmpty
        }
        
        It "Should handle comments" {
            $testFile = "$script:testFixtureDir/CommentTest.java"
            $content = @'
public class CommentTest {
    // This is a comment
    /* Multi-line
       comment */
    public void method() {
        // Inline comment
        System.out.println("test");
    }
}
'@
            Set-Content -Path $testFile -Value $content
            $result = Parse-JavaSource -FilePath $testFile
            $result.Methods | Where-Object { $_.Name -eq 'method' } | Should Not BeNullOrEmpty
        }
    }
}

Describe "Get-FXMLFields" {
    It "Should return only @FXML annotated fields" {
        $testFile = "$script:testFixtureDir/FXMLFieldsTest.java"
        $content = @'
public class Test {
    @FXML
    private TextField field1;
    
    private TextField field2;
    
    @FXML
    private Button button1;
}
'@
        Set-Content -Path $testFile -Value $content
        $parsed = Parse-JavaSource -FilePath $testFile
        $fxmlFields = Get-FXMLFields -ParsedSource $parsed
        $fxmlFields.Count | Should Be 2
    }
}

Describe "Get-UIEventHandlers" {
    It "Should identify event handler methods by naming pattern" {
        $testFile = "$script:testFixtureDir/EventHandlerTest.java"
        $content = @'
public class Test {
    public void handleClick() {
        System.out.println("clicked");
    }
    
    public void onButtonAction() {
        System.out.println("action");
    }
    
    public void regularMethod() {
        System.out.println("regular");
    }
}
'@
        Set-Content -Path $testFile -Value $content
        $parsed = Parse-JavaSource -FilePath $testFile
        $handlers = Get-UIEventHandlers -ParsedSource $parsed
        $handlers.Count | Should BeGreaterThan 1
    }
}

Describe "Find-DatabaseCalls" {
    It "Should find DatabaseHelper method calls" {
        $code = @'
public void loadData() {
    List<Resident> residents = DatabaseHelper.getAllResidents();
    DatabaseHelper.saveResident(resident);
}
'@
        $calls = Find-DatabaseCalls -CodeBlock $code
        $calls.Count | Should BeGreaterThan 1
    }
    
    It "Should find JDBC operations" {
        $code = @'
public void query() {
    Connection conn = getConnection();
    PreparedStatement pstmt = conn.prepareStatement(sql);
    ResultSet rs = pstmt.executeQuery();
}
'@
        $calls = Find-DatabaseCalls -CodeBlock $code
        $calls.Count | Should BeGreaterThan 0
    }
}
