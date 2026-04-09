# BDMS System - Runtime Testing & Validation Log

**System:** Barangay Documentation Management System  
**Version:** 1.0  
**Test Date:** April 9, 2026  
**Tester:** QA Team  
**Build:** Production Build #1  

---

## QUICK VALIDATION CHECKLIST

### ✅ Pre-Launch Verification
- [x] Project compiles: 0 errors, 0 warnings
- [x] Database H2 initialized at ~/bdms_v2
- [x] All dependencies resolved
- [x] JavaFX 17+ environment available
- [x] JDK 21 detected

### 📋 Critical Path Testing (Execute in Order)

#### 1. APPLICATION LAUNCH
```
Steps:
1. Run: java -cp target/classes com.example.Launcher
2. Verify: Main window appears with sidebar
3. Verify: Dark/Light theme toggle works
4. Expected: No console errors
```
**Result:** [ ] PASS [ ] FAIL  
**Notes:** _______________________________________________

---

#### 2. NAVIGATION INTEGRITY
```
Steps:
1. Click: Overview tab → Verify content loads
2. Click: Complaints & Incidents → Verify tab structure appears
3. Click: Announcements Portal → Verify filtering controls present
4. Click: Financial Reports → Verify tables visible
5. Click: Security Features → Verify 4-tab interface loads
6. Rapid switch: Click each tab 5 times in 10 seconds
7. Verify: No UI glitches, memory doesn't spike, no crashes
```
**Result:** [ ] PASS [ ] FAIL  
**Notes:** _______________________________________________

---

#### 3. ROLE-BASED ACCESS CONTROL
```
Test Case 3A: Super Admin Login
- Username: superadmin | Role: Super Admin
- Expected: ALL tabs accessible, NO restrictions
- Result: [ ] PASS [ ] FAIL

Test Case 3B: Secretary Login  
- Username: secretary | Role: Secretary
- Expected: Complaints, Announcements, Residents visible; Financial restricted
- Result: [ ] PASS [ ] FAIL

Test Case 3C: Treasurer Login
- Username: treasurer | Role: Treasurer
- Expected: Financial Reports prominent; Complaints read-only
- Result: [ ] PASS [ ] FAIL

Test Case 3D: Resident Login
- Username: resident | Role: Resident
- Expected: Submit Complaint allowed; Manage Complaints hidden; Finance hidden
- Result: [ ] PASS [ ] FAIL
```

**Overall RBAC:** [ ] PASS [ ] FAIL  
**Issues Found:** _______________________________________________

---

#### 4. COMPLAINTS & INCIDENTS - FULL WORKFLOW

```
Test Case 4.1: Resident Submits Complaint
Steps:
  a) Click: Complaints & Incidents tab
  b) Click: "Submit Complaint" sub-tab
  c) Fill: Title = "Street Pothole on Main St"
  d) Fill: Description = "Large pothole near the marketplace, safety hazard"
  e) Upload: Optional photo (test with and without)
  f) Click: Submit Complaint → Toast "Complaint submitted successfully"
  g) Open: Database query: SELECT * FROM complaints WHERE title='Street Pothole...'
  h) Verify: Record exists with correct dateSubmitted, status='Pending', residentName populated

Result: [ ] PASS [ ] FAIL
Database Confirmation: [ ] YES [ ] NO
```

**Test Case 4.2: Admin Manages Complaint**
```
Steps:
  a) Switch to: "Manage Complaints" sub-tab
  b) Verify: Newly submitted complaint appears in table
  c) Select: The complaint row
  d) Click: "View Details" → Dialog shows all complaint information
  e) Click: "Update Status" → Select "Ongoing" → Verify toast + refresh
  f) Database: SELECT status FROM complaints WHERE id=? → Should be "Ongoing"
  g) Click: "Add Notes" → Type "Assigned to maintenance team" → Save
  h) Database: SELECT adminNotes FROM complaints WHERE id=? → Should contain notes
  i) Click: "Generate Report" → PDF file created in ~/Downloads/
  j) Open PDF: Verify contains complaint details, summary statistics

Result: [ ] PASS [ ] FAIL
PDF Generated: [ ] YES [ ] NO
Database Updated: [ ] YES [ ] NO
```

---

#### 5. ANNOUNCEMENTS PORTAL - FULL WORKFLOW

```
Test Case 5.1: Post Three Announcements
Steps:
  a) Click: Announcements Portal tab → "Post Announcement" sub-tab
  b) Post #1 (Event):
     - Title: "Barangay Fiesta 2026"
     - Type: "Event"
     - Content: "Join us for the annual fiesta celebration"
     - Start: Today
     - End: +7 days
     - Submit → Toast confirms
  
  c) Post #2 (Emergency Alert):
     - Title: "Road Closure - Main Street"
     - Type: "Emergency Alert"  
     - Content: "Main Street closed for repair, use alternate routes"
     - Start: Today
     - End: +3 days
     - Submit → Toast confirms
  
  d) Post #3 (Program):
     - Title: "COVID-19 Vaccination Drive"
     - Type: "Program"
     - Content: "Free vaccination at barangay center, Mondays & Thursdays"
     - Start: Today
     - End: +30 days
     - Submit → Toast confirms

  e) Database: SELECT COUNT(*) FROM announcements WHERE status='Active' 
     → Should see +3 from before

Result: [ ] PASS [ ] FAIL
Records Created: [ ] 3 [ ] Less
```

**Test Case 5.2: Filter & Manage Announcements**
```
Steps:
  a) Click: "Manage Announcements" sub-tab
  b) Verify: All 3 announcements visible in table
  c) Filter by "Event" → Only post #1 visible
  d) Filter by "Emergency Alert" → Only post #2 visible
  e) Filter by "Program" → Only post #3 visible
  f) Filter by "All" → All 3 visible again
  g) Select post #2 → Click "Toggle Status" → Should change to "Inactive"
  h) Verify: Status column updated
  i) Database: SELECT status FROM announcements WHERE title LIKE 'Road Closure%' 
     → Should be "Inactive"
  j) Click: "Edit" on post #1 → Modify title to "Fiesta 2026 - A Grand Celebration"
  k) Save → Toast confirms → Table updated
  l) Select post #3 → Click "Delete" → Confirm → Toast confirms
  m) Database: SELECT COUNT(*) FROM announcements WHERE title LIKE '%Vaccination%'
     → Should be 0 (deleted)

Result: [ ] PASS [ ] FAIL
Filtering Works: [ ] YES [ ] NO
CRUD Operations: [ ] YES [ ] NO
```

**Test Case 5.3: Dashboard Integration**
```
Steps:
  a) Click: Overview tab
  b) Verify: Announcement statistics cards show (Events, Emergency Alerts, Programs)
  c) Event count should be 1, Alerts should be 0 (was toggled inactive), Programs should be 0
  d) Recent Announcements section shows latest posts
  e) Posts displayed with color badges indicating type
  f) Click back to Announcements → Post new announcement
  g) Return to Overview → Dashboard updates automatically (no manual refresh needed)

Result: [ ] PASS [ ] FAIL
Real-time Sync: [ ] YES [ ] NO
```

---

#### 6. FINANCIAL REPORTS - DATA INTEGRITY

```
Test Case 6.1: Verify Real Database Data
Steps:
  a) Database query: 
     SELECT request_date, SUM(fee) as daily_total 
     FROM document_requests 
     WHERE payment_status = 'PAID' AND request_date >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)
     GROUP BY request_date
  
  b) Note the results (dates and amounts)
  
  c) In app: Click Financial Reports tab
  
  d) Verify: Daily Collections table displays dates and amounts from step (a)
     → Should match exactly (not dummy random data)
  
  e) Verify: "Total Daily Collections" value matches SUM from query
  
  f) Database query:
     SELECT YEAR(request_date) as year, MONTH(request_date) as month, SUM(fee)
     FROM document_requests 
     WHERE payment_status = 'PAID' AND request_date >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH)
     GROUP BY YEAR(request_date), MONTH(request_date)
  
  g) Note the results (months and amounts)
  
  h) In app: Verify Monthly Income table matches query results
  
  i) Add new paid document_request:
     INSERT INTO document_requests (residentID, fee, paymentStatus, request_date) 
     VALUES (1, 500, 'PAID', CURRENT_DATE)
  
  j) Refresh Financial Reports (navigate away and back)
  
  k) Verify: New amount appears in Daily Collections
  
  l) Verify: Total updated correctly

Result: [ ] PASS [ ] FAIL  
Data Verified Real: [ ] YES [ ] NO
Real-time Updates: [ ] YES [ ] NO
```

**Test Case 6.2: PDF & CSV Export**
```
Steps:
  a) From Financial Reports tab:
  
  b) Click "Print Daily Report" → PDF created
     - File: ~/Downloads/Financial_Report_daily_2026-04-09.pdf
     - Verify: [ ] YES [ ] NO
     - Open PDF: Contains title, summary, table with dates/amounts
     - Verify: [ ] YES [ ] NO
  
  c) Click "Print Monthly Report" → PDF created
     - File: ~/Downloads/Financial_Report_monthly_2026-04-09.pdf
     - Verify: [ ] YES [ ] NO
     - Open PDF: Contains monthly breakdown
     - Verify: [ ] YES [ ] NO
  
  d) Click "Export to CSV" → CSV created
     - File: ~/Downloads/Financial_Data_2026-04-09.csv
     - Verify: [ ] YES [ ] NO
     - Open in Excel/Sheets: 
       * Section headers present: [ ] YES [ ] NO
       * Daily Collections data: [ ] YES [ ] NO
       * Monthly Income data: [ ] YES [ ] NO
       * Totals calculated: [ ] YES [ ] NO

Result: [ ] PASS [ ] FAIL
All Exports Created: [ ] YES [ ] NO
Content Correct: [ ] YES [ ] NO
```

---

#### 7. SECURITY FEATURES - UI & INTEGRATION

```
Test Case 7.1: User Authentication Tab
Steps:
  a) Click: Security Features tab → "User Authentication" tab
  b) Verify: Users table displays 4 users (superadmin, secretary, treasurer, resident)
  c) Select user: treasurer
  d) Click: "Change Password" → Toast appears
  e) Click: "Disable Account" → Toast appears
  f) Click: "Add User" → Toast appears (ready for backend)
  g) Info box shows statistics (Total Users: 4, Active Sessions: 1, Last Auth timestamp)

Result: [ ] PASS [ ] FAIL
UI Loads: [ ] YES [ ] NO
Buttons Functional: [ ] YES [ ] NO
```

**Test Case 7.2: Role-Based Access Tab**
```
Steps:
  a) Click: "Role-Based Access" tab
  b) Verify: 5 roles displayed (Super Admin, Secretary, Treasurer, Barangay Captain, Resident)
  c) Select role: Secretary
  d) Permissions box shows 4 colored badges:
     - Resident Data (green): [ ] YES [ ] NO
     - Financials (blue): [ ] YES [ ] NO
     - Blotter/Legal (amber): [ ] YES [ ] NO
     - System Settings (purple): [ ] YES [ ] NO
  e) Click: "Edit Permissions" → Toast (ready for backend)
  f) Click: "Create New Role" → Toast (ready for backend)

Result: [ ] PASS [ ] FAIL
Permissions Displayed: [ ] YES [ ] NO
Colors Correct: [ ] YES [ ] NO
```

**Test Case 7.3: Data Encryption Tab**
```
Steps:
  a) Click: "Data Encryption" tab
  b) Status card shows "● ENABLED" (green)
  c) 4 CheckBoxes present:
     - "Encrypt Resident Data" (checked): [ ] YES [ ] NO
     - "Encrypt Financial Records" (checked): [ ] YES [ ] NO
     - "Encrypt User Passwords" (checked): [ ] YES [ ] NO
     - "Encrypt Audit Logs" (unchecked): [ ] YES [ ] NO
  d) Key Management section:
     - Last Key Rotation date displayed: [ ] YES [ ] NO
     - "Rotate Encryption Keys" button present: [ ] YES [ ] NO
  e) Click: "Save Encryption Settings" → Toast confirms

Result: [ ] PASS [ ] FAIL
UI Complete: [ ] YES [ ] NO
```

**Test Case 7.4: Automatic Backups Tab**
```
Steps:
  a) Click: "Automatic Backups" tab
  b) Backup Schedule section:
     - ComboBox options (Hourly, Daily, Weekly, Monthly): [ ] YES [ ] NO
     - Default selected: Daily: [ ] YES [ ] NO
     - Backup Time display: 02:00 AM: [ ] YES [ ] NO
  c) Last Backup Status card:
     - Last Backup timestamp: [ ] YES [ ] NO
     - Backup Size: 245 MB: [ ] YES [ ] NO
     - Status: ✓ Success (green): [ ] YES [ ] NO
  d) Settings section:
     - Backup Location: ~/.../BDMS_Backups: [ ] YES [ ] NO
     - Retention: Keep last 30 backups: [ ] YES [ ] NO
  e) Action buttons:
     - "Backup Now": [ ] YES [ ] NO
     - "Restore Backup": [ ] YES [ ] NO
     - "View Backup Logs": [ ] YES [ ] NO
  f) Click each button → Toast confirms

Result: [ ] PASS [ ] FAIL
All Elements Present: [ ] YES [ ] NO
```

---

#### 8. THEME CONSISTENCY

```
Test Case 8.1: Dark/Light Mode Toggle
Steps:
  a) Navigate to each tab: Overview, Complaints, Announcements, Financial, Security
  b) With each tab open, toggle theme (Dark ↔ Light)
  c) Verify: ALL elements respect theme:
     - Backgrounds: [ ] YES [ ] NO
     - Text colors: [ ] YES [ ] NO
     - Table headers: [ ] YES [ ] NO
     - Form fields: [ ] YES [ ] NO
     - Buttons: [ ] YES [ ] NO
     - Cards and borders: [ ] YES [ ] NO
  d) No inconsistent colors or broken styling

Result: [ ] PASS [ ] FAIL
Consistent Across All Tabs: [ ] YES [ ] NO
No Styling Issues: [ ] YES [ ] NO
```

---

#### 9. PERFORMANCE & STABILITY

```
Test Case 9.1: Launch Time
- Start time: _____________
- Window visible: _____________
- Target: < 5 seconds
- Result: [ ] PASS [ ] FAIL
- Actual time: _____________ seconds
```

**Test Case 9.2: Rapid Tab Switching**
```
Steps:
  a) Open: Task Manager → Performance tab (monitor memory)
  b) Note: Initial memory usage
  c) Rapidly click through all tabs 20 times (as fast as possible)
  d) Measure: Does memory continuously increase or stay stable?
  e) Expected: Memory stable (no memory leak)
  
Result: [ ] PASS [ ] FAIL
Memory Stable: [ ] YES [ ] NO
Initial Memory: _____________ MB
After Test: _____________ MB
Difference: _____________ MB (should be < 10 MB)
```

**Test Case 9.3: Database Query Performance**
```
Steps:
  a) Financial Reports tab click → Measure load time
     Target: < 1 second
     Actual: _____________ seconds
     Result: [ ] PASS [ ] FAIL
  
  b) Complaints tab → Open manage tab → Measure load time
     Target: < 1 second
     Actual: _____________ seconds
     Result: [ ] PASS [ ] FAIL
  
  c) Announcements filter by type → Measure filter response
     Target: < 500ms
     Actual: _____________ ms
     Result: [ ] PASS [ ] FAIL
  
  d) Generate PDF report → Measure creation time
     Target: < 2 seconds
     Actual: _____________ seconds
     Result: [ ] PASS [ ] FAIL
```

---

#### 10. ERROR HANDLING & EDGE CASES

```
Test Case 10.1: Form Validation
Steps:
  a) Complaint submission with empty title → Submit button should be DISABLED
     Result: [ ] PASS [ ] FAIL
  
  b) Complaint with title only (no description) → Submit button DISABLED
     Result: [ ] PASS [ ] FAIL
  
  c) Announcement with empty required fields → Submit button DISABLED
     Result: [ ] PASS [ ] FAIL
  
  d) Try to update complaint without selecting row → "Please select..." toast
     Result: [ ] PASS [ ] FAIL
```

**Test Case 10.2: Concurrent Operations**
```
Steps:
  a) In one window: Submit complaint
  b) Simultaneously in another window: Post announcement
  c) Both should succeed without conflicts
     Result: [ ] PASS [ ] FAIL
  
  d) Rapidly refresh complaints table 10 times
  e) Verify no NullPointerExceptions in console
     Result: [ ] PASS [ ] FAIL
```

---

## SUMMARY REPORT

### Test Execution Statistics
- **Total Test Cases:** 30+
- **Passed:** _____ / _____
- **Failed:** _____ / _____
- **Skipped:** _____ / _____
- **Pass Rate:** _____%

### Critical Issues Found
```
Issue #1: ________________________________
Severity: [ ] CRITICAL [ ] HIGH [ ] MEDIUM [ ] LOW
Reproduction: ________________________________
Status: [ ] OPEN [ ] IN PROGRESS [ ] RESOLVED

Issue #2: ________________________________
Severity: [ ] CRITICAL [ ] HIGH [ ] MEDIUM [ ] LOW
Reproduction: ________________________________
Status: [ ] OPEN [ ] IN PROGRESS [ ] RESOLVED
```

### Blockers for Production
- [ ] No blockers found
- [ ] Blockers found (list above)

### Performance Summary
- **Average Tab Load Time:** _____________ ms
- **Average Query Response:** _____________ ms
- **Memory Usage (Stable):** _____________ MB
- **CPU Usage (Peak):** _____________ %

### Recommendations
1. ________________________________
2. ________________________________
3. ________________________________

### Sign-Off
- **Tested By:** ______________________ **Date:** __________
- **Reviewed By:** ______________________ **Date:** __________
- **Approved For Production:** [ ] YES [ ] NO

---

**Status:** READY FOR UAT [ ] | NEEDS FIXES [ ] | BLOCKED [ ]
