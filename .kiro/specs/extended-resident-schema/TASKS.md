# Extended Resident Schema - Implementation Tasks

## Task 1: Update Database Schema ⏳
**Priority:** High  
**Estimated Time:** 30 minutes  
**Dependencies:** None

### Description
Add new columns to the residents table to support the professor's CSV format.

### Changes Required
**File:** `src/main/java/com/example/DatabaseHelper.java`

Add the following ALTER TABLE statements in the `initializeDatabase()` method after the CREATE TABLE statements:

```java
// Add new columns for extended resident information
try {
    stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS family_id INTEGER");
} catch (SQLException ignored) { }

try {
    stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS house_unit VARCHAR(20)");
} catch (SQLException ignored) { }

try {
    stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS street VARCHAR(200)");
} catch (SQLException ignored) { }

try {
    stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS subdivision VARCHAR(200)");
} catch (SQLException ignored) { }

try {
    stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS gate_color VARCHAR(50)");
} catch (SQLException ignored) { }

try {
    stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS vaccination_count INTEGER DEFAULT 0");
} catch (SQLException ignored) { }

// Create index for family queries
try {
    stmt.execute("CREATE INDEX IF NOT EXISTS idx_residents_family_id ON residents(family_id)");
} catch (SQLException ignored) { }
```

### Acceptance Criteria
- [ ] All new columns added to residents table
- [ ] Index created on family_id
- [ ] Existing data remains intact
- [ ] No errors on database initialization

---

## Task 2: Update Resident Model ⏳
**Priority:** High  
**Estimated Time:** 45 minutes  
**Dependencies:** Task 1

### Description
Update the Resident.java model class to include new fields with JavaFX properties.

### Changes Required
**File:** `src/main/java/com/example/Resident.java`

Add new properties and methods:

```java
// Add to class fields
private final IntegerProperty familyId;
private final StringProperty houseUnit;
private final StringProperty street;
private final StringProperty subdivision;
private final StringProperty gateColor;
private final IntegerProperty vaccinationCount;

// Update constructor to include new fields
public Resident(int id, String firstName, String middleName, String lastName, 
                String birthDate, String gender, String address,
                Integer familyId, String houseUnit, String street, 
                String subdivision, String gateColor, Integer vaccinationCount) {
    // ... existing code ...
    this.familyId = new SimpleIntegerProperty(familyId != null ? familyId : 0);
    this.houseUnit = new SimpleStringProperty(houseUnit);
    this.street = new SimpleStringProperty(street);
    this.subdivision = new SimpleStringProperty(subdivision);
    this.gateColor = new SimpleStringProperty(gateColor);
    this.vaccinationCount = new SimpleIntegerProperty(vaccinationCount != null ? vaccinationCount : 0);
}

// Add getters and setters for all new fields
public Integer getFamilyId() { return familyId.get(); }
public void setFamilyId(Integer value) { familyId.set(value != null ? value : 0); }
public IntegerProperty familyIdProperty() { return familyId; }

public String getHouseUnit() { return houseUnit.get(); }
public void setHouseUnit(String value) { houseUnit.set(value); }
public StringProperty houseUnitProperty() { return houseUnit; }

public String getStreet() { return street.get(); }
public void setStreet(String value) { street.set(value); }
public StringProperty streetProperty() { return street; }

public String getSubdivision() { return subdivision.get(); }
public void setSubdivision(String value) { subdivision.set(value); }
public StringProperty subdivisionProperty() { return subdivision; }

public String getGateColor() { return gateColor.get(); }
public void setGateColor(String value) { gateColor.set(value); }
public StringProperty gateColorProperty() { return gateColor; }

public Integer getVaccinationCount() { return vaccinationCount.get(); }
public void setVaccinationCount(Integer value) { vaccinationCount.set(value != null ? value : 0); }
public IntegerProperty vaccinationCountProperty() { return vaccinationCount; }

// Add helper method to get formatted address
public String getDetailedAddress() {
    StringBuilder addr = new StringBuilder();
    if (houseUnit.get() != null && !houseUnit.get().isEmpty()) {
        addr.append(houseUnit.get());
    }
    if (street.get() != null && !street.get().isEmpty()) {
        if (addr.length() > 0) addr.append(", ");
        addr.append(street.get());
    }
    if (subdivision.get() != null && !subdivision.get().isEmpty()) {
        if (addr.length() > 0) addr.append(", ");
        addr.append(subdivision.get());
    }
    return addr.length() > 0 ? addr.toString() : getAddress();
}
```

### Acceptance Criteria
- [ ] All new fields added with JavaFX properties
- [ ] Getters and setters implemented
- [ ] Constructor updated
- [ ] Helper method for detailed address works
- [ ] No compilation errors

---

## Task 3: Create Professor CSV Import Method ⏳
**Priority:** High  
**Estimated Time:** 1 hour  
**Dependencies:** Task 1, Task 2

### Description
Create a specialized import method for the professor's CSV format.

### Changes Required
**File:** `src/main/java/com/example/DatabaseHelper.java`

Add new method:

```java
/**
 * Import residents from professor's CSV format
 * CSV Format: Res_ID,Res_LN,Res_FN,Res_MidN,Family_ID,House_Unit,Street,Subdivision,Gate_Color,Age,Vaccination_Count
 * @param csvFilePath Path to the CSV file
 * @return Map with "success" and "failed" counts
 */
public static Map<String, Integer> importResidentsFromProfessorCSV(String csvFilePath) {
    Map<String, Integer> result = new HashMap<>();
    int successCount = 0;
    int failedCount = 0;
    
    String insertSQL = "INSERT INTO residents (first_name, middle_name, last_name, birth_date, gender, address, " +
                      "family_id, house_unit, street, subdivision, gate_color, vaccination_count, role, image_path) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(insertSQL);
         java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(csvFilePath))) {
        
        String line;
        boolean isFirstLine = true;
        int currentYear = java.time.Year.now().getValue();
        
        while ((line = br.readLine()) != null) {
            // Skip header
            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }
            
            if (line.trim().isEmpty()) continue;
            
            try {
                String[] fields = line.split(",");
                
                if (fields.length < 11) {
                    System.err.println("Skipping invalid line: " + line);
                    failedCount++;
                    continue;
                }
                
                // Parse fields
                String lastName = fields[1].trim();
                String firstName = fields[2].trim();
                String middleName = fields[3].trim();
                int familyId = Integer.parseInt(fields[4].trim());
                String houseUnit = fields[5].trim();
                String street = fields[6].trim();
                String subdivision = fields[7].trim();
                String gateColor = fields[8].trim();
                int age = Integer.parseInt(fields[9].trim());
                int vaccinationCount = Integer.parseInt(fields[10].trim());
                
                // Calculate birth date from age (approximation)
                int birthYear = currentYear - age;
                String birthDate = birthYear + "-01-01";
                
                // Construct full address
                String fullAddress = houseUnit + ", " + street + ", " + subdivision;
                
                // Set parameters
                pstmt.setString(1, firstName);
                pstmt.setString(2, middleName);
                pstmt.setString(3, lastName);
                pstmt.setString(4, birthDate);
                pstmt.setString(5, null); // gender (not in CSV)
                pstmt.setString(6, fullAddress);
                pstmt.setInt(7, familyId);
                pstmt.setString(8, houseUnit);
                pstmt.setString(9, street);
                pstmt.setString(10, subdivision);
                pstmt.setString(11, gateColor);
                pstmt.setInt(12, vaccinationCount);
                pstmt.setString(13, "Resident");
                pstmt.setString(14, getDefaultResidentImagePath());
                
                pstmt.executeUpdate();
                successCount++;
                
            } catch (Exception e) {
                System.err.println("Failed to import line: " + line);
                System.err.println("Error: " + e.getMessage());
                failedCount++;
            }
        }
        
        // Log import
        if (successCount > 0) {
            logAction("System", 
                     String.format("Imported %d residents from professor's CSV. Failed: %d", successCount, failedCount),
                     "CSV Import", "Resident");
        }
        
    } catch (Exception e) {
        System.err.println("Error during CSV import: " + e.getMessage());
        e.printStackTrace();
    }
    
    result.put("success", successCount);
    result.put("failed", failedCount);
    return result;
}
```

### Acceptance Criteria
- [ ] Method imports all 100 residents from professor's CSV
- [ ] Family IDs preserved correctly
- [ ] Addresses constructed properly
- [ ] Birth dates calculated from age
- [ ] Vaccination counts stored
- [ ] Error handling works
- [ ] Audit log entry created

---

## Task 4: Remove Duplicate Import Method ⏳
**Priority:** Medium  
**Estimated Time:** 15 minutes  
**Dependencies:** Task 3

### Description
Remove the duplicate `importResidentsFromCSV()` method that was just added.

### Changes Required
**File:** `src/main/java/com/example/DatabaseHelper.java`

- Remove lines 1695-1805 (the duplicate method)
- Keep the existing `bulkImportResidentsFromCSV()` method for backward compatibility
- Keep the new `importResidentsFromProfessorCSV()` method

### Acceptance Criteria
- [ ] Duplicate method removed
- [ ] No compilation errors
- [ ] Existing import functionality still works

---

## Task 5: Update DatabaseHelper Read Methods ⏳
**Priority:** Medium  
**Estimated Time:** 30 minutes  
**Dependencies:** Task 1, Task 2

### Description
Update all methods that read residents from the database to include new fields.

### Changes Required
**File:** `src/main/java/com/example/DatabaseHelper.java`

Update these methods:
- `getResidents()` - Add new fields to SELECT and Resident constructor
- `getResidentById()` - Add new fields to SELECT and Resident constructor
- `addResident()` - Add new fields to INSERT
- `updateResident()` - Add new fields to UPDATE

Example for `getResidents()`:
```java
String sql = "SELECT id, first_name, middle_name, last_name, birth_date, gender, address, " +
             "image_path, role, family_id, house_unit, street, subdivision, gate_color, vaccination_count " +
             "FROM residents ...";

// In ResultSet processing:
Resident resident = new Resident(
    rs.getInt("id"),
    rs.getString("first_name"),
    rs.getString("middle_name"),
    rs.getString("last_name"),
    rs.getString("birth_date"),
    rs.getString("gender"),
    rs.getString("address"),
    rs.getInt("family_id"),
    rs.getString("house_unit"),
    rs.getString("street"),
    rs.getString("subdivision"),
    rs.getString("gate_color"),
    rs.getInt("vaccination_count")
);
```

### Acceptance Criteria
- [ ] All read methods include new fields
- [ ] All write methods include new fields
- [ ] No SQL errors
- [ ] Existing residents display correctly

---

## Task 6: Add UI Import Button (Optional) ⏳
**Priority:** Low  
**Estimated Time:** 30 minutes  
**Dependencies:** Task 3

### Description
Add a button in the UI to import the professor's CSV format.

### Changes Required
**File:** `src/main/java/com/example/App.java`

In the Residents section, add a new button:

```java
var importProfessorCSVButton = new Button("Import Professor CSV", new FontIcon(FontAwesomeSolid.FILE_UPLOAD));
importProfessorCSVButton.getStyleClass().add("button-secondary");
importProfessorCSVButton.setOnAction(e -> {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Professor's CSV File");
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("CSV Files", "*.csv")
    );
    File file = fileChooser.showOpenDialog(primaryStage);
    
    if (file != null) {
        Map<String, Integer> result = DatabaseHelper.importResidentsFromProfessorCSV(file.getAbsolutePath());
        int success = result.get("success");
        int failed = result.get("failed");
        
        showAlert("Import Complete", 
                 String.format("Successfully imported %d residents.\nFailed: %d", success, failed));
        
        // Refresh table
        refreshResidentTable();
    }
});
```

### Acceptance Criteria
- [ ] Button appears in Residents section
- [ ] File chooser opens on click
- [ ] Import executes successfully
- [ ] Success message displays
- [ ] Table refreshes with new data

---

## Task 7: Testing ⏳
**Priority:** High  
**Estimated Time:** 1 hour  
**Dependencies:** All previous tasks

### Test Cases

1. **Schema Migration Test**
   - [ ] Run application with existing database
   - [ ] Verify new columns added
   - [ ] Verify existing data intact
   - [ ] Verify index created

2. **Professor CSV Import Test**
   - [ ] Import resident-records.csv
   - [ ] Verify all 100 residents imported
   - [ ] Check family_id values (1-20)
   - [ ] Verify addresses constructed correctly
   - [ ] Check vaccination counts (3-5)
   - [ ] Verify gate colors stored

3. **Data Integrity Test**
   - [ ] Query residents by family_id
   - [ ] Verify family grouping works
   - [ ] Check address components
   - [ ] Verify birth dates calculated correctly

4. **Backward Compatibility Test**
   - [ ] Existing residents display correctly
   - [ ] Old CSV import still works
   - [ ] UI handles null values for new fields

5. **Error Handling Test**
   - [ ] Import CSV with missing fields
   - [ ] Import CSV with invalid data
   - [ ] Verify error messages
   - [ ] Check failed count accurate

### Acceptance Criteria
- [ ] All test cases pass
- [ ] No data loss
- [ ] No SQL errors
- [ ] Performance acceptable

---

## Summary

**Total Estimated Time:** 4-5 hours

**Task Priority Order:**
1. Task 1: Update Database Schema (CRITICAL)
2. Task 2: Update Resident Model (CRITICAL)
3. Task 3: Create Professor CSV Import Method (CRITICAL)
4. Task 5: Update DatabaseHelper Read Methods (HIGH)
5. Task 4: Remove Duplicate Import Method (MEDIUM)
6. Task 7: Testing (HIGH)
7. Task 6: Add UI Import Button (LOW - Optional)

**Ready to Start:** Task 1 (no dependencies)
