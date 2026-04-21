# Editable Role Permissions - Configuration Guide

## ✅ Dynamic Permission Management System

The Role Permissions system is now fully editable! Administrators can change permissions for any role, and those changes are saved to the database and applied system-wide.

---

## 🎯 How It Works

### 1. **Database-Backed Permissions**
- Permissions are stored in the `role_permissions` table
- Each role-module combination has a permission level
- Changes persist across application restarts

### 2. **Editable Interface**
- Click any permission cell to change it
- Dropdown shows: None, View Only, Manage, Full Access
- Color-coded cells for easy identification
- Save button to commit all changes

### 3. **Immediate Feedback**
- Changes are highlighted in the table
- Save confirmation message
- Requires restart for changes to take effect

---

## 📋 How to Change Permissions

### Step 1: Access Role Permissions
1. Login as **Super Admin**, **Owner**, or **Barangay Captain**
2. Click **"User & Access"** in the sidebar
3. Click **"Role Permissions"** tab

### Step 2: Edit Permissions
1. **Click on any permission cell** (except Role column)
2. A dropdown appears with 4 options:
   - **None** - No access (module hidden)
   - **View Only** - Read-only access
   - **Manage** - Create, edit, delete
   - **Full Access** - Complete control

3. **Select the new permission level**
4. The cell updates immediately

### Step 3: Save Changes
1. Click the **"Save All Changes"** button at the bottom
2. Wait for confirmation message
3. **Restart the application** for changes to take effect

---

## 🎨 Visual Indicators

Permissions are color-coded for easy identification:

| Permission | Color | Background | Text Color |
|-----------|-------|------------|------------|
| **Full Access** | Green | Light Green (#d1fae5) | Dark Green (#065f46) |
| **Manage** | Blue | Light Blue (#dbeafe) | Dark Blue (#1e40af) |
| **View Only** | Yellow | Light Yellow (#fef3c7) | Dark Brown (#92400e) |
| **None** | Red | Light Red (#fee2e2) | Dark Red (#991b1b) |

---

## 💡 Example Use Cases

### Use Case 1: Give Treasurer Access to Residents
**Scenario:** Treasurer needs to view resident data for financial reports

**Steps:**
1. Go to Role Permissions tab
2. Find "Barangay Treasurer" row
3. Click on "Residents" cell
4. Change from "View Only" to "Manage"
5. Click "Save All Changes"
6. Restart application

**Result:** Treasurer can now manage resident data

---

### Use Case 2: Restrict Secretary from Financial Reports
**Scenario:** Secretary should not see financial data

**Steps:**
1. Go to Role Permissions tab
2. Find "Barangay Secretary" row
3. Click on "Financial" cell
4. Change from "View Only" to "None"
5. Click "Save All Changes"
6. Restart application

**Result:** Financial Reports menu hidden for Secretary

---

### Use Case 3: Create Custom Role Permissions
**Scenario:** New role "Assistant" needs specific access

**Steps:**
1. Go to "Manage Roles" tab
2. Click "Add Role"
3. Create role "Assistant"
4. Go to "Role Permissions" tab
5. Find "Assistant" row
6. Set permissions for each module:
   - Analytics: View Only
   - Residents: Manage
   - Certificates: Manage
   - Everything else: None
7. Click "Save All Changes"
8. Restart application

**Result:** Assistant role has custom permissions

---

## 🔐 Permission Levels Explained

### None
- **Menu:** Hidden completely
- **Access:** No access to module
- **Use for:** Modules the role should never see

### View Only
- **Menu:** Visible
- **Access:** Can view data, cannot edit
- **Use for:** Roles that need to see but not modify

### Manage
- **Menu:** Visible
- **Access:** Can create, read, update, delete
- **Use for:** Roles that actively work with the module

### Full Access
- **Menu:** Visible
- **Access:** Complete control including configuration
- **Use for:** Administrators and managers

---

## ⚠️ Important Notes

### 1. Restart Required
- Changes are saved to database immediately
- **Application must be restarted** for changes to take effect
- This ensures all users get the updated permissions

### 2. Super Admin Protection
- Always keep at least one role with Full Access to User & Access
- Otherwise, you may lock yourself out of permission management

### 3. Current Users
- Changes affect all users with that role
- Users currently logged in won't see changes until they log out and back in

### 4. Default Permissions
- Default permissions are loaded on first run
- After that, database permissions take precedence
- You can always reset by clearing the `role_permissions` table

---

## 🛠️ Technical Details

### Database Schema
```sql
CREATE TABLE role_permissions (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(100) NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    permission_level VARCHAR(20) NOT NULL,
    UNIQUE(role_name, module_name)
);
```

### Permission Flow
1. User logs in → Role retrieved
2. System calls `getPermissions(role)`
3. Method checks database first
4. If not in database, uses hardcoded defaults
5. Permissions applied to UI

### Saving Permissions
```java
DatabaseHelper.savePermission(roleName, moduleName, permissionLevel);
```
- Uses MERGE to update or insert
- Atomic operation per permission
- Immediate database commit

---

## 🧪 Testing Permissions

### Test Scenario 1: Change and Verify
1. Login as Super Admin
2. Change Tanod's "Residents" from "View Only" to "Manage"
3. Save changes
4. Restart application
5. Login as Tanod
6. Verify: Can now add/edit residents

### Test Scenario 2: Hide Module
1. Login as Super Admin
2. Change Health Worker's "Announcements" from "View Only" to "None"
3. Save changes
4. Restart application
5. Login as Health Worker
6. Verify: Announcements menu is hidden

---

## 📊 Default Permissions Matrix

| Role | Analytics | Users | Residents | Certificates | Complaints | Announcements | Financial | Security | System | Maintenance |
|------|-----------|-------|-----------|--------------|------------|---------------|-----------|----------|--------|-------------|
| Super Admin | Full | Full | Full | Full | Full | Full | Full | Full | Full | Full |
| Owner | Full | Manage | Full | Full | Manage | Manage | Manage | Manage | Manage | View |
| Barangay Captain | Full | Full | Full | Full | Full | Full | Full | Full | Full | Full |
| Barangay Secretary | View | None | Manage | Manage | Manage | Manage | View | None | None | None |
| Barangay Treasurer | View | None | View | View | None | View | Manage | None | None | None |
| Kagawads | View | None | View | View | View | View | View | None | None | None |
| Health Worker | None | None | Manage | None | None | View | None | None | None | None |
| Tanod | None | None | View | None | Manage | View | None | None | None | None |

---

## ✅ Summary

**What You Can Do:**
- ✓ Click any permission cell to edit
- ✓ Choose from 4 permission levels
- ✓ Save changes to database
- ✓ Changes persist across restarts
- ✓ Customize permissions for any role
- ✓ Create custom role configurations

**What Happens:**
- ✓ Changes saved to `role_permissions` table
- ✓ System loads from database on startup
- ✓ UI reflects new permissions after restart
- ✓ All users with that role get updated permissions

**Remember:**
- ⚠️ Always restart after saving changes
- ⚠️ Keep at least one admin role
- ⚠️ Test changes with a non-admin account first

---

**Last Updated:** April 20, 2026  
**Feature:** Editable Role Permissions  
**Status:** ✅ Fully Functional
