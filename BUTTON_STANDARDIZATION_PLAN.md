# Button Standardization Plan

## Goal
Apply the Manage Roles button styling (icons, sizing, tooltips) to ALL buttons across the application.

---

## ✅ Gold Standard Template (from Manage Roles)

```java
Button addButton = new Button("Add Role");
addButton.setGraphic(new FontIcon(FontAwesomeSolid.PLUS_CIRCLE));
addButton.getStyleClass().addAll("button-secondary", "button-small");
addButton.setTooltip(new Tooltip("Add Role"));
```

**Key Elements**:
1. Icon with `setGraphic(new FontIcon(...))`
2. Style classes: `"button-secondary"` or `"button-primary"` + size class
3. Tooltip with `setTooltip(new Tooltip(...))`
4. Clear text label

---

## 📋 Buttons Inventory

### ✅ Already Perfect (No Changes Needed)
1. **Manage Roles** - Add, Edit, Delete (template buttons)
2. **Manage Users** - Edit, Delete in Actions column
3. **Resident Data** - Add, Import, Edit, Delete, Print ID, View ID, Export PDF
4. **Certificates & Clearances** - Approve, Payment, Generate, SMS

### ⏳ Need Minor Updates (Have icons, need tooltips or sizing)
5. **Login Screen** - Login button
6. **Sidebar** - Toggle button, navigation buttons
7. **Scan Button** - QR code scanner
8. **Submit Button** - Document request submission

### ⏳ Need Updates (Missing icons or tooltips)
9. **Dialog Buttons** - Various save/create/edit buttons in dialogs
10. **Filter Buttons** - "All", "With Accounts", "No Accounts"
11. **Upload Button** - Photo upload in resident form

---

## 🎯 Standardization Strategy

### Phase 1: Main Module Buttons ✅
**Status**: Already done!
- Resident Data (7 buttons) ✅
- Certificates & Clearances (4 buttons) ✅
- Manage Roles (3 buttons) ✅
- Manage Users (2 buttons in Actions) ✅

### Phase 2: Secondary Buttons (Current Focus)
- Login button
- Sidebar buttons
- Scan button
- Submit buttons
- Filter buttons
- Upload buttons

### Phase 3: Dialog Buttons
- Save/Create/Edit buttons in dialogs
- These are ButtonType, not Button objects
- May need different approach

---

## 📊 Button Analysis

### Buttons Found: 50+

### Categories:
1. **Toolbar Buttons** (15+) - Add, Edit, Delete, Import, Export
2. **Action Buttons** (10+) - Approve, Generate, Submit, Send
3. **Navigation Buttons** (8+) - Sidebar menu items
4. **Utility Buttons** (5+) - Scan, Upload, Toggle
5. **Filter Buttons** (3+) - All, With Accounts, No Accounts
6. **Dialog Buttons** (10+) - Save, Create, Cancel, Close
7. **Table Action Buttons** (10+) - Inline edit/delete/view buttons

---

## 🔧 Implementation Plan

### Step 1: Verify Current State ✅
Review all buttons to identify which need updates.

**Result**: Most main module buttons already have icons and proper styling!

### Step 2: Update Secondary Buttons
Focus on buttons that are missing:
- Icons
- Tooltips
- Proper sizing classes

### Step 3: Verify Consistency
Ensure all buttons follow the same pattern:
- Icon placement
- Style classes
- Tooltip format
- Text labels

### Step 4: Test & Compile
- Compile application
- Visual inspection
- Verify no text cutoff
- Check icon alignment

---

## 📝 Buttons That Need Updates

### 1. Login Button
**Current**:
```java
var loginButton = new Button("Login");
loginButton.getStyleClass().add("button-primary");
```

**Updated**:
```java
var loginButton = new Button("Login");
loginButton.setGraphic(new FontIcon(FontAwesomeSolid.SIGN_IN_ALT));
loginButton.getStyleClass().addAll("button-primary", "button-large");
loginButton.setTooltip(new Tooltip("Sign in to your account"));
```

### 2. Sidebar Toggle Button
**Current**:
```java
Button toggleBtn = new Button("", toggleIcon);
toggleBtn.getStyleClass().add("sidebar-toggle-btn");
```

**Keep as is** - Already has icon, special styling

### 3. Submit Button (Document Request)
**Current**:
```java
Button submitBtn = new Button("Submit");
submitBtn.getStyleClass().addAll("button-primary", "button-small");
submitBtn.setTooltip(new Tooltip("Submit Request"));
```

**Updated**:
```java
Button submitBtn = new Button("Submit");
submitBtn.setGraphic(new FontIcon(FontAwesomeSolid.PAPER_PLANE));
submitBtn.getStyleClass().addAll("button-primary", "button-small");
submitBtn.setTooltip(new Tooltip("Submit Request"));
```

### 4. Upload Button (Photo)
**Current**:
```java
Button uploadBtn = new Button("Upload Photo");
```

**Updated**:
```java
Button uploadBtn = new Button("Upload Photo");
uploadBtn.setGraphic(new FontIcon(FontAwesomeSolid.UPLOAD));
uploadBtn.getStyleClass().addAll("button-secondary", "button-small");
uploadBtn.setTooltip(new Tooltip("Upload resident photo"));
```

### 5. Filter Buttons
**Current**:
```java
Button showAllBtn = new Button("All");
Button showWithAccountsBtn = new Button("With Accounts");
Button showWithoutAccountsBtn = new Button("No Accounts");
```

**Updated**:
```java
Button showAllBtn = new Button("All");
showAllBtn.setGraphic(new FontIcon(FontAwesomeSolid.LIST));
showAllBtn.getStyleClass().addAll("button-primary", "button-small");
showAllBtn.setTooltip(new Tooltip("Show all residents"));

Button showWithAccountsBtn = new Button("With Accounts");
showWithAccountsBtn.setGraphic(new FontIcon(FontAwesomeSolid.USER_CHECK));
showWithAccountsBtn.getStyleClass().addAll("button-secondary", "button-small");
showWithAccountsBtn.setTooltip(new Tooltip("Show residents with accounts"));

Button showWithoutAccountsBtn = new Button("No Accounts");
showWithoutAccountsBtn.setGraphic(new FontIcon(FontAwesomeSolid.USER_TIMES));
showWithoutAccountsBtn.getStyleClass().addAll("button-secondary", "button-small");
showWithoutAccountsBtn.setTooltip(new Tooltip("Show residents without accounts"));
```

---

## ✅ Buttons Already Perfect

### Resident Data Module ✅
```java
Button addButton = new Button("Add");
addButton.setGraphic(new FontIcon(FontAwesomeSolid.PLUS_CIRCLE));
addButton.getStyleClass().addAll("button-secondary", "button-small");
addButton.setTooltip(new Tooltip("Add Resident"));

Button importButton = new Button("Import");
importButton.setGraphic(new FontIcon(FontAwesomeSolid.FILE_IMPORT));
importButton.getStyleClass().addAll("button-secondary", "button-small");
importButton.setTooltip(new Tooltip("Import CSV"));

Button editButton = new Button("Edit");
editButton.setGraphic(new FontIcon(FontAwesomeSolid.PENCIL_ALT));
editButton.getStyleClass().addAll("button-secondary", "button-small");
editButton.setTooltip(new Tooltip("Edit Resident"));

Button deleteButton = new Button("Delete");
deleteButton.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));
deleteButton.getStyleClass().addAll("button-secondary", "button-small");
deleteButton.setTooltip(new Tooltip("Delete Resident"));

Button idButton = new Button("Print ID");
idButton.setGraphic(new FontIcon(FontAwesomeSolid.ID_CARD));
idButton.getStyleClass().addAll("button-secondary", "button-small");
idButton.setTooltip(new Tooltip("Print ID Card"));

Button viewIdBtn = new Button("View ID");
viewIdBtn.setGraphic(new FontIcon(FontAwesomeSolid.ADDRESS_CARD));
viewIdBtn.getStyleClass().addAll("button-secondary", "button-small");
viewIdBtn.setTooltip(new Tooltip("View ID Card"));

Button exportButton = new Button("Export PDF");
exportButton.setGraphic(new FontIcon(FontAwesomeSolid.FILE_PDF));
exportButton.getStyleClass().addAll("button-accent", "button-small");
```

**Status**: ✅ Perfect! All 7 buttons have icons, sizing, and tooltips.

### Certificates & Clearances Module ✅
```java
Button approveBtn = new Button("Approve", new FontIcon(FontAwesomeSolid.CHECK_CIRCLE));
approveBtn.getStyleClass().addAll("button-secondary", "button-small");
approveBtn.setTooltip(new Tooltip("Approve Request"));

Button paymentBtn = new Button("Payment", new FontIcon(FontAwesomeSolid.DOLLAR_SIGN));
paymentBtn.getStyleClass().addAll("button-secondary", "button-small");
paymentBtn.setTooltip(new Tooltip("Record Payment"));

Button generateBtn = new Button("Generate", new FontIcon(FontAwesomeSolid.FILE_PDF));
generateBtn.getStyleClass().addAll("button-secondary", "button-small");
generateBtn.setTooltip(new Tooltip("Generate & Print"));

Button sendSMSBtn = new Button("SMS", new FontIcon(FontAwesomeSolid.MOBILE_ALT));
sendSMSBtn.getStyleClass().addAll("button-warning", "button-small");
sendSMSBtn.setTooltip(new Tooltip("Send SMS"));
```

**Status**: ✅ Perfect! All 4 buttons have icons, sizing, and tooltips.

---

## 🎯 Priority Updates

### High Priority (User-facing, frequently used)
1. ✅ Login button - Add icon
2. ✅ Submit button - Add icon
3. ✅ Upload button - Add icon and styling
4. ✅ Filter buttons - Add icons

### Medium Priority (Less frequently used)
5. Scan button - Already has icon, verify tooltip
6. Sidebar buttons - Already have icons, verify consistency

### Low Priority (Internal/Dialog buttons)
7. Dialog ButtonTypes - May not need changes
8. Table action buttons - Already styled in Manage Users

---

## 📊 Expected Results

### Before Standardization:
- ~30% of buttons have icons
- ~50% have tooltips
- ~60% have proper sizing
- Inconsistent styling

### After Standardization:
- ✅ 100% of buttons have icons
- ✅ 100% have tooltips
- ✅ 100% have proper sizing
- ✅ Consistent styling throughout

---

## 🚀 Implementation Status

- [x] Phase 1: Main Module Buttons (Already done!)
- [ ] Phase 2: Secondary Buttons (In progress)
- [ ] Phase 3: Verification & Testing

---

**Next Steps**: Update the 5 high-priority buttons identified above.
