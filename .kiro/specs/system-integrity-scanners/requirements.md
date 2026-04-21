# Requirements Document: System Integrity Scanners

## Introduction

The System Integrity Verification Solution provides automated quality assurance for the Barangay Data Management System (BDMS). The solution consists of two PowerShell scanner scripts that analyze the JavaFX application's frontend (App.java) and backend (DatabaseHelper.java) components to detect errors, inconsistencies, and potential reliability issues. These scanners serve as pre-deployment verification tools and post-change validation mechanisms.

## Glossary

- **Frontend_Scanner**: PowerShell script that analyzes UI/UX components in App.java
- **Backend_Scanner**: PowerShell script that analyzes data integrity and database operations in DatabaseHelper.java
- **BDMS**: Barangay Data Management System - the JavaFX application being analyzed
- **Issue_Report**: Structured output document containing detected problems with severity classifications
- **Severity_Level**: Classification of issues as Critical, High, Medium, or Low priority
- **Pattern_Matcher**: Regular expression or code analysis logic that identifies specific bug patterns
- **Steering_File**: Documentation file that describes expected system behavior for verification
- **Source_Parser**: Component that reads and analyzes Java source code files
- **Data_Flow_Analyzer**: Component that traces data movement between UI and database layers
- **Event_Handler**: JavaFX code that responds to user interactions (button clicks, form submissions)
- **State_Manager**: Code responsible for maintaining UI element states (enabled/disabled, visible/hidden)
- **Validation_Rule**: Business logic that ensures data meets required constraints before persistence
- **UI_Thread**: JavaFX Application Thread responsible for rendering UI updates (must not be blocked by long operations)
- **Platform.runLater()**: JavaFX method that schedules UI updates to run on the UI_Thread safely
- **FXML_File**: XML file defining JavaFX UI layout structure
- **FXML_ID**: Unique identifier linking FXML UI elements to Java controller fields
- **Absolute_Path**: File path starting from root directory (e.g., C:\Users\...) that breaks portability
- **Relative_Path**: File path relative to application root or classpath that maintains portability
- **JDBC_Connection_String**: Database URL containing connection parameters and configuration settings

## Requirements

### Requirement 1: Frontend Scanner Creation

**User Story:** As a QA engineer, I want a PowerShell script that scans App.java for UI/UX issues, so that I can identify frontend problems before deployment.

#### Acceptance Criteria

1. THE Frontend_Scanner SHALL parse App.java source code and extract UI component definitions
2. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL analyze all button event handlers for orphaned or missing action implementations
3. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL detect UI elements with incorrect state management (disabled when should be enabled)
4. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL identify table components with data binding issues or empty row patterns
5. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL verify UI update mechanisms after data changes (refresh/reload patterns)
6. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL check for label/icon mismatches with function behavior
7. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL detect duplicate entries in lists or menu structures
8. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL identify functions that exhibit state corruption (work once, fail on second execution)
9. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL verify form clearing logic after submission
10. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL check navigation active indicator updates
11. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL detect modal/dialog closing issues
12. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL verify search functionality filtering correctness
13. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL check pagination display accuracy
14. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL verify sort functionality on all table columns
15. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL detect silent failures in export/print operations
16. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL check image upload/display for broken image patterns
17. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL verify QR code generation/scanning functionality
18. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL detect toast notification display and stacking issues
19. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL verify theme switching applies to all components
20. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL identify responsive layout issues (overlapping or cut-off elements)
21. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL check accessibility compliance (tooltips, labels, keyboard navigation)

### Requirement 2: Backend Scanner Creation

**User Story:** As a QA engineer, I want a PowerShell script that scans DatabaseHelper.java for data integrity issues, so that I can identify backend problems before deployment.

#### Acceptance Criteria

1. THE Backend_Scanner SHALL parse DatabaseHelper.java source code and extract database operation methods
2. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL verify query result consistency with expected database content
3. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL detect validation gaps allowing invalid data persistence
4. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL identify foreign key violation risks and orphaned record patterns
5. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL detect duplicate record creation patterns
6. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL verify data persistence after save operations
7. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL check data type and format conversion correctness
8. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL identify SQL injection vulnerabilities in query construction
9. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL verify audit log entries for critical operations
10. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL check permission validation for data access operations
11. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL detect database connection leaks and timeout issues
12. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL verify transaction rollback handling
13. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL check cascade delete implementation correctness
14. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL identify date/time format inconsistencies
15. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL detect null pointer exception risks from missing data
16. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL verify aggregation calculation correctness (counts, sums, averages)

### Requirement 3: Performance Issue Detection

**User Story:** As a QA engineer, I want both scanners to detect performance issues, so that I can ensure responsive user experience.

#### Acceptance Criteria

1. WHEN either scanner is executed, THE scanner SHALL identify operations taking longer than 2 seconds to respond
2. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL detect UI thread blocking during database operations
3. WHEN either scanner is executed, THE scanner SHALL identify memory leak patterns from unclosed resources
4. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL detect inefficient query patterns causing slowdowns
5. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL identify large result sets without proper pagination

### Requirement 4: Integration Issue Detection

**User Story:** As a QA engineer, I want both scanners to detect integration issues, so that I can ensure external component compatibility.

#### Acceptance Criteria

1. WHEN either scanner is executed, THE scanner SHALL detect CSV import failure patterns and data corruption risks
2. WHEN either scanner is executed, THE scanner SHALL identify PDF generation error patterns
3. WHEN either scanner is executed, THE scanner SHALL verify QR code encoding/decoding consistency
4. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL detect webcam integration failure patterns
5. WHEN either scanner is executed, THE scanner SHALL identify file path resolution issues and missing resource patterns

### Requirement 5: System Configuration Issue Detection

**User Story:** As a QA engineer, I want both scanners to detect system configuration issues, so that I can ensure deployment readiness.

#### Acceptance Criteria

1. WHEN either scanner is executed, THE scanner SHALL verify required files and folders exist
2. WHEN either scanner is executed, THE scanner SHALL check file permission correctness
3. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL detect database schema mismatches
4. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL verify CSS style completeness
5. WHEN either scanner is executed, THE scanner SHALL identify broken resource paths in production configuration

### Requirement 6: Issue Report Generation

**User Story:** As a QA engineer, I want detailed reports from both scanners, so that I can prioritize and fix identified issues.

#### Acceptance Criteria

1. WHEN a scanner completes execution, THE scanner SHALL generate an Issue_Report containing all detected problems
2. THE Issue_Report SHALL categorize each issue by Severity_Level (Critical, High, Medium, Low)
3. THE Issue_Report SHALL include line numbers for each detected issue
4. THE Issue_Report SHALL provide suggested fixes for each detected issue
5. THE Issue_Report SHALL include a summary count of issues by Severity_Level
6. THE Issue_Report SHALL be formatted as human-readable text with clear section headers
7. THE Issue_Report SHALL include timestamp of scan execution
8. THE Issue_Report SHALL include scanner version information

### Requirement 7: Steering File Generation

**User Story:** As a QA engineer, I want steering files documenting expected behavior, so that I can maintain verification standards over time.

#### Acceptance Criteria

1. WHEN the Frontend_Scanner is executed for the first time, THE Frontend_Scanner SHALL generate a steering file documenting expected UI behavior patterns
2. WHEN the Backend_Scanner is executed for the first time, THE Backend_Scanner SHALL generate a steering file documenting expected data flow patterns
3. THE steering files SHALL include examples of correct implementation patterns
4. THE steering files SHALL include examples of anti-patterns to avoid
5. THE steering files SHALL be stored in markdown format for easy editing
6. THE steering files SHALL be versioned to track changes over time

### Requirement 8: Scanner Execution Interface

**User Story:** As a QA engineer, I want simple command-line execution of scanners, so that I can integrate them into deployment workflows.

#### Acceptance Criteria

1. THE Frontend_Scanner SHALL be executable from PowerShell command line with no required parameters
2. THE Backend_Scanner SHALL be executable from PowerShell command line with no required parameters
3. WHEN a scanner is executed, THE scanner SHALL display progress indicators during analysis
4. WHEN a scanner completes successfully, THE scanner SHALL exit with status code 0
5. WHEN a scanner encounters critical errors, THE scanner SHALL exit with non-zero status code
6. THE scanners SHALL accept optional parameter for output report file path
7. THE scanners SHALL accept optional parameter for verbosity level (quiet, normal, verbose)
8. WHEN a scanner is executed with invalid parameters, THE scanner SHALL display usage help message

### Requirement 9: Data Flow Consistency Verification

**User Story:** As a QA engineer, I want verification of data flow between UI and database, so that I can ensure end-to-end correctness.

#### Acceptance Criteria

1. WHEN both scanners are executed, THE scanners SHALL verify that UI form fields map correctly to database columns
2. WHEN both scanners are executed, THE scanners SHALL verify that database query results map correctly to UI table columns
3. WHEN both scanners are executed, THE scanners SHALL detect mismatches between UI validation rules and database constraints
4. WHEN both scanners are executed, THE scanners SHALL verify that UI actions trigger corresponding database operations
5. WHEN both scanners are executed, THE scanners SHALL detect data transformation errors between layers

### Requirement 10: Pattern Library Maintenance

**User Story:** As a QA engineer, I want scanners to use maintainable pattern libraries, so that I can extend detection capabilities over time.

#### Acceptance Criteria

1. THE scanners SHALL load Pattern_Matcher definitions from external configuration files
2. THE pattern configuration files SHALL be in JSON format for easy editing
3. WHEN a pattern configuration file is updated, THE scanners SHALL use the updated patterns on next execution without code changes
4. THE pattern configuration files SHALL include pattern name, description, severity level, and detection regex
5. THE scanners SHALL validate pattern configuration files before execution and report syntax errors

### Requirement 11: False Positive Suppression

**User Story:** As a QA engineer, I want to suppress known false positives, so that I can focus on genuine issues.

#### Acceptance Criteria

1. THE scanners SHALL support suppression comments in source code (e.g., `// SCANNER-IGNORE: reason`)
2. WHEN a suppression comment is encountered, THE scanner SHALL skip the flagged issue and note the suppression in the report
3. THE scanners SHALL support suppression configuration files listing line numbers to ignore
4. THE Issue_Report SHALL include a section listing all suppressed issues with their reasons
5. THE scanners SHALL warn when suppression comments reference non-existent issues

### Requirement 12: Incremental Scanning Support

**User Story:** As a developer, I want to scan only changed files, so that I can get fast feedback during development.

#### Acceptance Criteria

1. THE scanners SHALL accept optional parameter specifying file modification timestamp threshold
2. WHEN a timestamp threshold is provided, THE scanners SHALL only analyze files modified after that timestamp
3. WHEN incremental scanning is used, THE Issue_Report SHALL indicate that partial analysis was performed
4. THE scanners SHALL maintain a cache of previous scan results for comparison
5. WHEN incremental scanning is used, THE Issue_Report SHALL highlight new issues since last full scan

### Requirement 13: JavaFX UI Thread Safety Verification

**User Story:** As a JavaFX developer, I want the Frontend_Scanner to detect UI thread violations, so that I can prevent UI freezing and ensure responsive user experience.

#### Acceptance Criteria

1. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL identify database method calls in App.java that are not wrapped in Platform.runLater()
2. WHEN the Frontend_Scanner detects a database call updating UI elements without Platform.runLater(), THE Frontend_Scanner SHALL flag it as a Critical severity issue
3. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL verify that long-running operations (file I/O, network calls, database queries) execute on background threads
4. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL detect direct UI updates from non-UI threads
5. THE Issue_Report SHALL provide code examples showing correct Platform.runLater() usage for flagged violations
6. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL identify Task or Service usage patterns and verify proper UI update handling

### Requirement 14: File Path Portability Verification

**User Story:** As a deployment engineer, I want both scanners to detect hardcoded file paths, so that I can ensure the application runs correctly across different environments.

#### Acceptance Criteria

1. WHEN either scanner is executed, THE scanner SHALL identify all Absolute_Path references in the source code
2. WHEN an Absolute_Path is detected (e.g., C:\Users\Project\..., /home/user/...), THE scanner SHALL flag it as a High severity issue
3. THE Issue_Report SHALL suggest using getResource() for classpath resources as an alternative to Absolute_Path
4. THE Issue_Report SHALL suggest using Relative_Path or system properties (user.home, user.dir) as alternatives to Absolute_Path
5. WHEN either scanner is executed, THE scanner SHALL verify that resource loading uses getClass().getResourceAsStream() or getClass().getResource()
6. WHEN either scanner is executed, THE scanner SHALL detect File constructor calls with hardcoded paths and suggest Path.of() with relative paths
7. THE scanners SHALL allow suppression of Absolute_Path warnings for legitimate use cases (e.g., user-selected file paths)

### Requirement 15: JDBC Connection Configuration Verification

**User Story:** As a database administrator, I want the Backend_Scanner to verify JDBC connection settings, so that I can ensure proper database access configuration for development and production.

#### Acceptance Criteria

1. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL parse the JDBC_Connection_String in DatabaseHelper.java
2. WHEN the JDBC_Connection_String is for H2 database, THE Backend_Scanner SHALL verify it includes AUTO_SERVER=TRUE for shared access during debugging
3. WHEN the JDBC_Connection_String is missing AUTO_SERVER=TRUE, THE Backend_Scanner SHALL flag it as a Medium severity issue with explanation of debugging limitations
4. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL verify the JDBC_Connection_String includes DB_CLOSE_DELAY=-1 to prevent premature database closure
5. WHEN the Backend_Scanner is executed, THE Backend_Scanner SHALL check for hardcoded database credentials in the connection string
6. WHEN hardcoded credentials are detected, THE Backend_Scanner SHALL flag it as a Critical severity issue and suggest using environment variables or configuration files
7. THE Issue_Report SHALL provide examples of secure JDBC_Connection_String configurations for different deployment scenarios

### Requirement 16: FXML Controller Binding Verification

**User Story:** As a JavaFX developer, I want the Frontend_Scanner to verify FXML bindings, so that I can detect mismatches between controller fields and FXML layout definitions.

#### Acceptance Criteria

1. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL identify all .fxml files referenced in App.java
2. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL parse each FXML_File and extract all fx:id attributes
3. WHEN the Frontend_Scanner is executed, THE Frontend_Scanner SHALL identify all @FXML annotated fields in App.java
4. WHEN an @FXML field in App.java has no corresponding fx:id in any FXML_File, THE Frontend_Scanner SHALL flag it as a High severity issue
5. WHEN an fx:id in an FXML_File has no corresponding @FXML field in App.java, THE Frontend_Scanner SHALL flag it as a Medium severity issue
6. THE Issue_Report SHALL list all unmatched @FXML fields with their line numbers in App.java
7. THE Issue_Report SHALL list all unmatched fx:id attributes with their FXML_File names and line numbers
8. WHEN the Frontend_Scanner detects type mismatches between @FXML field types and FXML element types, THE Frontend_Scanner SHALL flag it as a Critical severity issue
9. THE scanners SHALL handle cases where FXML files are loaded dynamically or programmatically without direct references
