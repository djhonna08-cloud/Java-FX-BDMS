# Barangay San Marino BDMS - Login Credentials

## Default User Accounts

Below are the default login credentials for all roles in the system. These accounts are automatically created when the database is initialized.

---

### 🔐 Administrative Accounts

#### Super Admin
- **Username:** `superadmin`
- **Password:** `admin123`
- **Role:** Super Admin
- **Permissions:** Full access to all system features
- **Description:** System administrator with complete control over all modules

#### Owner
- **Username:** `owner`
- **Password:** `owner123`
- **Role:** Owner
- **Permissions:** Full access to most features, limited maintenance access
- **Description:** System owner with comprehensive management capabilities

---

### 👔 Barangay Officials

#### Barangay Captain
- **Username:** `captain`
- **Password:** `captain123`
- **Role:** Barangay Captain
- **Permissions:** Full access to all barangay operations
- **Description:** Head of the barangay with full administrative access

#### Barangay Secretary
- **Username:** `secretary`
- **Password:** `secretary123`
- **Role:** Barangay Secretary
- **Permissions:** Manage residents, certificates, complaints, and announcements
- **Description:** Manages resident data, legal cases, and correspondence

#### Barangay Treasurer
- **Username:** `treasurer`
- **Password:** `treasurer123`
- **Role:** Barangay Treasurer
- **Permissions:** Full access to financial reports, view-only for other modules
- **Description:** Manages financial records and budgets

#### Kagawad (Council Member)
- **Username:** `kagawad`
- **Password:** `kagawad123`
- **Role:** Kagawads
- **Permissions:** View-only access to most modules
- **Description:** Barangay council member with limited access

---

### 👥 Barangay Staff

#### Health Worker
- **Username:** `healthworker`
- **Password:** `health123`
- **Role:** Barangay Health Workers
- **Permissions:** Manage resident data, view announcements
- **Description:** Manages health-related resident information

#### Tanod (Security Personnel)
- **Username:** `tanod`
- **Password:** `tanod123`
- **Role:** Barangay Tanods
- **Permissions:** Manage complaints & incidents, view residents
- **Description:** Handles security and incident management

---

## Permission Matrix

| Role | Analytics | Users | Residents | Certificates | Complaints | Announcements | Financial | Security | System | Maintenance |
|------|-----------|-------|-----------|--------------|------------|---------------|-----------|----------|--------|-------------|
| **Super Admin** | Full | Full | Full | Full | Full | Full | Full | Full | Full | Full |
| **Owner** | Full | Manage | Full | Full | Manage | Manage | Manage | Manage | Manage | View |
| **Barangay Captain** | Full | Full | Full | Full | Full | Full | Full | Full | Full | Full |
| **Barangay Secretary** | View | None | Manage | Manage | Manage | Manage | View | None | None | None |
| **Barangay Treasurer** | View | None | View | View | None | View | Manage | None | None | None |
| **Kagawads** | View | None | View | View | View | View | View | None | None | None |
| **Health Workers** | None | None | Manage | None | None | View | None | None | None | None |
| **Tanods** | None | None | View | None | Manage | View | None | None | None | None |

### Permission Levels Explained:
- **Full Access:** Complete control - create, read, update, delete, and configure
- **Manage:** Create, read, update, and delete records
- **View Only:** Read-only access to view information
- **None:** No access to the module

---

## Security Notes

⚠️ **IMPORTANT SECURITY RECOMMENDATIONS:**

1. **Change Default Passwords:** All default passwords should be changed immediately after first login
2. **Strong Passwords:** Use passwords with at least 8 characters, including uppercase, lowercase, numbers, and special characters
3. **Regular Updates:** Update passwords regularly (every 90 days recommended)
4. **Access Control:** Only grant necessary permissions to each role
5. **Audit Logs:** Regularly review audit logs for suspicious activity
6. **User Management:** Remove or disable accounts for users who no longer need access

---

## First-Time Setup

1. **Login as Super Admin:**
   - Username: `superadmin`
   - Password: `admin123`

2. **Change Your Password:**
   - Navigate to User & Access → User Authentication
   - Update your password immediately

3. **Create Additional Users:**
   - Navigate to User & Access → Manage Roles
   - Add users as needed for your barangay

4. **Configure Permissions:**
   - Navigate to User & Access → Role Permissions
   - Review and adjust permissions as needed

---

## Troubleshooting

### Cannot Login?
- Verify username and password are correct (case-sensitive)
- Check if the account is active
- Contact the system administrator

### Forgot Password?
- Contact the Super Admin or Owner to reset your password
- They can update your password through User & Access → User Authentication

### Access Denied?
- Your role may not have permission for that module
- Contact your administrator to request access
- Review the Permission Matrix above

---

## Support

For technical support or questions about the system:
- Contact your Barangay IT Administrator
- Refer to the system documentation
- Check the User Manual for detailed instructions

---

**Document Version:** 1.0  
**Last Updated:** April 20, 2026  
**System:** Barangay San Marino BDMS
