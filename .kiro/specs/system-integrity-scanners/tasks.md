# Implementation Plan: System Integrity Scanners

## Overview

This implementation plan covers the development of two PowerShell scanner scripts (`frontend-scanner.ps1` and `backend-scanner.ps1`) that analyze JavaFX code for 50+ issue types. The scanners will perform static code analysis on App.java and DatabaseHelper.java, detecting UI/UX issues, thread safety violations, SQL injection risks, FXML binding mismatches, and configuration problems. The implementation includes pattern-based detection engines, JSON configuration systems, comprehensive reporting, and Pester-based testing.

## Tasks

- [x] 1. Set up project structure and core utilities
  - Create directory structure for scanner scripts, pattern configs, test fixtures, and documentation
  - Create shared utility module `ScannerUtils.psm1` with common functions (file validation, logging, progress display)
  - Set up Pester testing framework and test directory structure
  - Create `.gitignore` for cache files and test outputs
  - _Requirements: 8.1, 8.2, 8.3_

- [-] 2. Implement Java source parser
  - [x] 2.1 Create `Parse-JavaSource` function in PowerShell
    - Implement line-by-line parsing to extract imports, fields, methods, and annotations
    - Extract method bodies with proper brace matching for nested structures
    - Parse field declarations with type information and annotations (including `@FXML`)
    - Identify FXML file references using regex patterns
    - Return structured hashtable with parsed elements and line numbers
    - _Requirements: 1.1, 2.1_
  
  - [x] 2.2 Write unit tests for Java source parser
    - Test method extraction with various signatures (public/private, static, generic types)
    - Test field extraction with annotations (`@FXML`, `@Inject`)
    - Test import parsing and FXML reference detection
    - Test edge cases (nested classes, lambda expressions, comments)
    - _Requirements: 1.1, 2.1_

- [~] 3. Implement FXML parser
  - [x] 3.1 Create `Parse-FXMLFile` function in PowerShell
    - Parse FXML files as XML using `[xml]` type accelerator
    - Extract all elements with `fx:id` attributes using XPath
    - Extract element types and line numbers
    - Handle XML parsing errors gracefully with try-catch
    - Return structured array of FXML elements with IDs, types, and locations
    - _Requirements: 1.1, 16.1, 16.2, 16.3_
  
  - [x] 3.2 Write unit tests for FXML parser
    - Test fx:id extraction from valid FXML files
    - Test element type detection (TableView, TextField, Button, etc.)
    - Test error handling for malformed XML
    - Test handling of FXML files with namespaces
    - _Requirements: 16.1, 16.2, 16.3_

- [~] 4. Implement pattern configuration system
  - [x] 4.1 Create pattern configuration JSON files
    - Create `frontend-patterns.json` with 21+ frontend issue patterns (UI thread violations, FXML mismatches, file paths, etc.)
    - Create `backend-patterns.json` with 16+ backend issue patterns (SQL injection, JDBC config, validation gaps, resource leaks)
    - Define pattern schema: id, name, category, severity, regex, context_check, fix_suggestion, example_correct
    - Include all patterns from design document (FE-001 through FE-021, BE-001 through BE-016)
    - _Requirements: 10.1, 10.2, 10.4_
  
  - [x] 4.2 Create `Load-PatternConfig` function
    - Read JSON configuration file using `ConvertFrom-Json`
    - Validate required fields (id, name, severity, regex)
    - Validate regex patterns by attempting compilation
    - Return array of pattern objects or fall back to built-in defaults on error
    - Log warnings for invalid patterns
    - _Requirements: 10.1, 10.3, 10.5_
  
  - [~] 4.3 Write unit tests for pattern configuration
    - Test loading valid pattern configuration
    - Test validation of required fields
    - Test regex validation (detect invalid regex patterns)
    - Test fallback to defaults on configuration errors
    - _Requirements: 10.1, 10.3, 10.5_

- [~] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [~] 6. Implement UI thread safety analyzer
  - [~] 6.1 Create `Detect-UIThreadViolations` function
    - Identify UI event handler methods (names matching `handle*`, `on*`, or containing `setOnAction`)
    - Search method bodies for `DatabaseHelper.*` method calls
    - Check if database calls are wrapped in `Platform.runLater()` or executed in `Task`/`Service`
    - Extract line numbers for violations using substring analysis
    - Generate issue objects with severity=Critical, including code context and fix suggestions
    - _Requirements: 1.2, 1.8, 3.2, 13.1, 13.2, 13.3, 13.4, 13.5, 13.6_
  
  - [~] 6.2 Write unit tests for UI thread safety analyzer
    - Test detection of database calls without `Platform.runLater()`
    - Test that properly wrapped calls are not flagged (false positive prevention)
    - Test detection of background thread usage (`Task`, `Service`)
    - Test line number accuracy for violations
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

- [~] 7. Implement FXML binding analyzer
  - [~] 7.1 Create `Detect-FXMLBindingMismatches` function
    - Extract `@FXML` annotated fields from parsed Java source
    - Parse all FXML files in specified directory to extract `fx:id` attributes
    - Cross-reference Java fields with FXML IDs (case-sensitive matching)
    - Detect orphaned Java fields (no matching FXML ID) - High severity
    - Detect orphaned FXML IDs (no matching Java field) - Medium severity
    - Detect type mismatches between field type and FXML element type - Critical severity
    - Generate issues with appropriate severity levels and fix suggestions
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8_
  
  - [~] 7.2 Write unit tests for FXML binding analyzer
    - Test detection of orphaned `@FXML` fields
    - Test detection of orphaned `fx:id` attributes
    - Test detection of type mismatches
    - Test that correctly matched bindings are not flagged
    - _Requirements: 16.4, 16.5, 16.6, 16.7, 16.8_

- [~] 8. Implement SQL injection analyzer
  - [~] 8.1 Create `Detect-SQLInjectionRisks` function
    - Search for SQL query strings containing SELECT, INSERT, UPDATE, DELETE keywords
    - Detect string concatenation using `+` operator or `String.format()` with variables
    - Verify if queries use `PreparedStatement` with parameterized queries
    - Check for whitelisting or sanitization of concatenated values
    - Generate Critical severity issues for confirmed injection risks with fix suggestions
    - _Requirements: 2.8, 1.3_
  
  - [~] 8.2 Write unit tests for SQL injection analyzer
    - Test detection of string concatenation in SQL queries
    - Test that `PreparedStatement` usage is not flagged (false positive prevention)
    - Test detection of `String.format()` with variables
    - Test whitelisting detection (enum/constant values)
    - _Requirements: 2.8_

- [~] 9. Implement JDBC configuration analyzer
  - [~] 9.1 Create `Detect-JDBCConfigIssues` function
    - Parse JDBC connection strings from source code
    - Check H2 connection strings for `AUTO_SERVER=TRUE` parameter - Medium severity if missing
    - Check for `DB_CLOSE_DELAY=-1` parameter
    - Detect hardcoded credentials (USER=, PASS=, PASSWORD=) - Critical severity
    - Generate issues with fix suggestions for secure configuration
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7_
  
  - [~] 9.2 Write unit tests for JDBC configuration analyzer
    - Test detection of missing `AUTO_SERVER=TRUE`
    - Test detection of hardcoded credentials
    - Test that environment variable usage is not flagged
    - Test connection string parsing for H2 database
    - _Requirements: 15.2, 15.3, 15.5, 15.6_

- [~] 10. Implement file path portability analyzer
  - [~] 10.1 Create `Detect-FilePathIssues` function
    - Detect absolute paths using regex (C:, D:, /home/, /Users/)
    - Flag absolute paths as High severity issues
    - Detect `File` constructor calls with hardcoded paths
    - Verify usage of `getResource()` or `getResourceAsStream()` for classpath resources
    - Generate issues with suggestions for relative paths or `getResource()` usage
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_
  
  - [~] 10.2 Write unit tests for file path portability analyzer
    - Test detection of Windows absolute paths (C:\, D:\)
    - Test detection of Unix absolute paths (/home/, /Users/)
    - Test that `getResource()` usage is not flagged
    - Test that relative paths are not flagged
    - _Requirements: 14.1, 14.2, 14.5_

- [~] 11. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [~] 12. Implement pattern-based issue detection
  - [~] 12.1 Create `Detect-PatternIssues` function
    - Load patterns from configuration
    - Apply regex patterns to source code line-by-line
    - For patterns with `context_check`, verify context before flagging
    - Extract code context (5 lines before/after) for each match
    - Generate issue objects with pattern metadata (severity, fix suggestion, example)
    - Handle regex timeout to prevent ReDoS attacks
    - _Requirements: 1.2-1.21, 2.2-2.16, 3.1-3.5, 4.1-4.5, 5.1-5.5_
  
  - [~] 12.2 Write unit tests for pattern-based detection
    - Test regex pattern matching for various issue types
    - Test context checking for conditional patterns
    - Test code context extraction
    - Test regex timeout handling
    - _Requirements: 1.2-1.21, 2.2-2.16_

- [~] 13. Implement suppression system
  - [~] 13.1 Create suppression configuration schema
    - Define JSON schema for suppressions: file, line, pattern_id, reason, added_by, added_date
    - Create example `scan-suppressions.json` file
    - _Requirements: 11.1, 11.3_
  
  - [~] 13.2 Create `Load-SuppressionConfig` function
    - Read suppression configuration JSON file
    - Validate required fields (file, line, pattern_id, reason)
    - Return array of suppression objects
    - Handle missing or malformed configuration gracefully
    - _Requirements: 11.1, 11.3_
  
  - [~] 13.3 Create `Apply-Suppressions` function
    - Match issues against suppression rules by file, line, and pattern_id
    - Mark suppressed issues with `Suppressed = $true` and include reason
    - Detect suppression comments in source code (`// SCANNER-IGNORE: reason`)
    - Warn when suppressions reference non-existent issues
    - _Requirements: 11.1, 11.2, 11.4, 11.5_
  
  - [~] 13.4 Write unit tests for suppression system
    - Test loading valid suppression configuration
    - Test matching suppressions to issues
    - Test detection of suppression comments in source code
    - Test warnings for invalid suppressions
    - _Requirements: 11.1, 11.2, 11.3, 11.5_

- [~] 14. Implement report generation
  - [~] 14.1 Create `Generate-Report` function
    - Sort issues by severity (Critical, High, Medium, Low) then by line number
    - Format report with clear section headers and separators
    - Include summary statistics (issue counts by severity)
    - Include metadata (timestamp, scanner version, file analyzed, scan mode)
    - Format code context with line numbers and highlighting for flagged line
    - Include fix suggestions and correct implementation examples
    - Add section for suppressed issues with reasons
    - Write formatted report to output file
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 11.4_
  
  - [~] 14.2 Write unit tests for report generation
    - Test report formatting with various issue types
    - Test summary statistics calculation
    - Test sorting by severity and line number
    - Test suppressed issues section
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [~] 15. Implement steering file generation
  - [~] 15.1 Create `Generate-SteeringFile` function
    - Generate markdown documentation for expected behavior patterns
    - Include examples of correct implementations for each issue category
    - Include examples of anti-patterns to avoid
    - Add version information and timestamp
    - Write to `frontend-steering.md` or `backend-steering.md`
    - Only generate on first execution (check if file exists)
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  
  - [~] 15.2 Write unit tests for steering file generation
    - Test markdown formatting
    - Test inclusion of correct implementation examples
    - Test inclusion of anti-patterns
    - Test file existence check (only generate once)
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [~] 16. Implement incremental scanning
  - [~] 16.1 Create cache management functions
    - Create `Save-Cache` function to write scan results to JSON cache file
    - Create `Load-Cache` function to read previous scan results
    - Include timestamp and scanner version in cache
    - _Requirements: 12.4_
  
  - [~] 16.2 Create `Compare-ScanResults` function
    - Compare current issues with previous issues from cache
    - Mark issues as NEW (not in previous), CHANGED (different details), or RESOLVED (in previous but not current)
    - Return categorized issue arrays
    - _Requirements: 12.5_
  
  - [~] 16.3 Implement incremental mode in scanner scripts
    - Check file modification timestamp against threshold
    - Load cached results if file not modified
    - Perform full scan if file modified
    - Generate incremental report highlighting NEW, CHANGED, RESOLVED issues
    - Update cache after scan
    - _Requirements: 12.1, 12.2, 12.3, 12.5_
  
  - [~] 16.4 Write unit tests for incremental scanning
    - Test cache save and load operations
    - Test issue comparison (NEW, CHANGED, RESOLVED)
    - Test timestamp checking
    - Test incremental report generation
    - _Requirements: 12.1, 12.2, 12.3, 12.5_

- [~] 17. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [~] 18. Implement frontend scanner script
  - [~] 18.1 Create `frontend-scanner.ps1` main script
    - Define command-line parameters (SourceFile, FXMLDirectory, OutputFile, Verbosity, Incremental, etc.)
    - Implement main execution flow: validate inputs, load configs, parse source, run analyzers, apply suppressions, generate report
    - Add progress indicators using `Write-Progress`
    - Implement error handling with try-catch and appropriate exit codes (0=success, 1=issues found, 2=execution error)
    - Add verbose logging for debugging
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8_
  
  - [~] 18.2 Integrate all frontend analyzers
    - Call `Detect-UIThreadViolations` for thread safety analysis
    - Call `Detect-FXMLBindingMismatches` for FXML binding verification
    - Call `Detect-PatternIssues` for pattern-based detection (event handlers, state management, integration issues)
    - Call `Detect-FilePathIssues` for portability verification
    - Aggregate all issues into single array
    - _Requirements: 1.1-1.21, 13.1-13.6, 14.1-14.7, 16.1-16.9_
  
  - [~] 18.3 Write integration tests for frontend scanner
    - Test end-to-end execution with sample App.java file
    - Test detection of all 21+ frontend issue types
    - Test report generation and exit codes
    - Test incremental scanning mode
    - Test error handling (file not found, parse errors)
    - _Requirements: 1.1-1.21, 8.1-8.8_

- [~] 19. Implement backend scanner script
  - [~] 19.1 Create `backend-scanner.ps1` main script
    - Define command-line parameters (SourceFile, OutputFile, Verbosity, Incremental, etc.)
    - Implement main execution flow: validate inputs, load configs, parse source, run analyzers, apply suppressions, generate report
    - Add progress indicators using `Write-Progress`
    - Implement error handling with try-catch and appropriate exit codes
    - Add verbose logging for debugging
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8_
  
  - [~] 19.2 Integrate all backend analyzers
    - Call `Detect-SQLInjectionRisks` for SQL security analysis
    - Call `Detect-JDBCConfigIssues` for JDBC configuration verification
    - Call `Detect-PatternIssues` for pattern-based detection (validation gaps, resource leaks, transaction issues)
    - Call `Detect-FilePathIssues` for portability verification
    - Aggregate all issues into single array
    - _Requirements: 2.1-2.16, 15.1-15.7_
  
  - [~] 19.3 Write integration tests for backend scanner
    - Test end-to-end execution with sample DatabaseHelper.java file
    - Test detection of all 16+ backend issue types
    - Test report generation and exit codes
    - Test incremental scanning mode
    - Test error handling (file not found, parse errors)
    - _Requirements: 2.1-2.16, 8.1-8.8_

- [~] 20. Implement data flow consistency analyzer
  - [~] 20.1 Create `Verify-DataFlowConsistency` function
    - Build UI field map from frontend parsed source (TextField, TextArea, ComboBox, DatePicker)
    - Build database column map from backend parsed source (CREATE TABLE statements)
    - Trace UI-to-database flow by matching field names to column names
    - Trace database-to-UI flow by matching query results to table columns
    - Detect validation mismatches (UI allows empty but DB has NOT NULL)
    - Detect type mismatches (UI TextField → DB INTEGER without parsing)
    - Detect length mismatches (UI max length > DB column length)
    - Generate issues for each inconsistency with both UI and DB line numbers
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
  
  - [~] 20.2 Write unit tests for data flow analyzer
    - Test UI field map building
    - Test database column map building
    - Test validation mismatch detection
    - Test type mismatch detection
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [~] 21. Create comprehensive test fixtures
  - [~] 21.1 Create test fixture files
    - Create `test-fixtures/app-with-thread-violation.java` with FE-001 issue
    - Create `test-fixtures/app-with-orphaned-field.java` with FXML binding issue
    - Create `test-fixtures/app-correct.java` with no issues (false positive test)
    - Create `test-fixtures/database-with-sql-injection.java` with BE-001 issue
    - Create `test-fixtures/database-correct.java` with no issues
    - Create `test-fixtures/comprehensive-issues.java` with all 50+ issue types
    - Create `test-fixtures/fxml/` directory with sample FXML files
    - _Requirements: All requirements for regression testing_
  
  - [~] 21.2 Write regression tests for all issue types
    - Create Pester test suite that verifies detection of all 21 frontend issue types
    - Create Pester test suite that verifies detection of all 16 backend issue types
    - Create Pester test suite that verifies detection of all 5 performance issue types
    - Create Pester test suite that verifies detection of all 5 integration issue types
    - Create Pester test suite that verifies detection of all 5 system configuration issue types
    - Use comprehensive test fixture file to verify all patterns work correctly
    - _Requirements: 1.1-1.21, 2.1-2.16, 3.1-3.5, 4.1-4.5, 5.1-5.5_

- [~] 22. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [~] 23. Create documentation and examples
  - [~] 23.1 Create README.md
    - Document installation prerequisites (PowerShell version, OS compatibility)
    - Document usage examples for both scanners
    - Document command-line parameters and options
    - Document exit codes and their meanings
    - Document pattern configuration format
    - Document suppression configuration format
    - _Requirements: 8.1-8.8, 10.1-10.5, 11.1-11.5_
  
  - [~] 23.2 Create CI/CD integration examples
    - Create GitHub Actions workflow example
    - Create pre-commit hook example
    - Create Jenkins pipeline example
    - Document integration best practices
    - _Requirements: 8.4, 8.5_
  
  - [~] 23.3 Create pattern library documentation
    - Document all 21 frontend patterns with examples
    - Document all 16 backend patterns with examples
    - Document how to add new patterns
    - Document how to modify existing patterns
    - _Requirements: 10.1-10.5_

- [~] 24. Implement error handling and logging
  - [~] 24.1 Add comprehensive error handling
    - Handle file not found errors with clear messages and exit code 2
    - Handle parse errors with warnings and partial analysis continuation
    - Handle pattern configuration errors with fallback to defaults
    - Handle FXML processing errors with graceful degradation
    - Handle memory/performance issues with timeouts and warnings
    - Handle suppression configuration errors with warnings
    - Handle cache errors with fallback to full scan
    - _Requirements: All error handling requirements from design_
  
  - [~] 24.2 Write error handling tests
    - Test file not found error handling
    - Test parse error handling and partial analysis
    - Test pattern configuration error handling
    - Test FXML processing error handling
    - _Requirements: Error handling requirements_

- [~] 25. Performance optimization
  - [~] 25.1 Optimize parsing performance
    - Implement streaming file reading for large files
    - Add regex compilation caching
    - Optimize brace matching algorithm for method extraction
    - Add progress indicators for long-running operations
    - _Requirements: 3.1-3.5_
  
  - [~] 25.2 Add performance monitoring
    - Add timing measurements for each analyzer
    - Log performance warnings for large files (>5000 lines)
    - Add timeout limits for regex matching (prevent ReDoS)
    - _Requirements: 3.1-3.5_
  
  - [~] 25.3 Write performance tests
    - Test analysis of 5000-line file completes in under 10 seconds
    - Test handling of 100 FXML files without memory issues
    - Test regex timeout handling
    - _Requirements: 3.1-3.5_

- [~] 26. Final integration and validation
  - [~] 26.1 Run scanners on actual BDMS codebase
    - Execute `frontend-scanner.ps1` on actual `App.java`
    - Execute `backend-scanner.ps1` on actual `DatabaseHelper.java`
    - Review all reported issues for false positives
    - Verify all known issues are detected (no false negatives)
    - _Requirements: All requirements_
  
  - [~] 26.2 Create release package
    - Package scanner scripts, pattern configs, documentation
    - Create installation script
    - Create version file with release notes
    - Test installation on clean system
    - _Requirements: 8.1-8.8_

- [~] 27. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- All tasks involve writing, modifying, or testing PowerShell code
- Tasks marked with `*` are optional testing tasks that can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation throughout implementation
- The scanners use pattern-based static analysis, not property-based testing
- PowerShell was chosen as the implementation language for native Windows integration and scripting capabilities
- Test fixtures provide concrete examples for regression testing
- Error handling ensures graceful degradation and clear user feedback
