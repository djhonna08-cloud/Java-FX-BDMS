# Fix Login Issue - Update Credentials

The new credentials only apply to fresh databases. If you have an existing database, you need to update the user passwords. Here are **3 easy methods** to fix the login issue:

---

## 🚀 Method 1: Run PowerShell Script (EASIEST)

1. **Right-click** on `Update-Credentials.ps1`
2. Select **"Run with PowerShell"**
3. Wait for the script to complete
4. Try logging in with the new credentials

**New Credentials:**
- `superadmin` / `admin123`
- `captain` / `captain123`
- `secretary` / `secretary123`
- `treasurer` / `treasurer123`
- etc.

---

## 🔧 Method 2: Use H2 Console (RECOMMENDED)

1. **Start H2 Console:**
   - Double-click `StartH2Console.java` OR
   - Run: `java -cp "%USERPROFILE%\.m2\repository\com\h2database\h2\2.2.220\h2-2.2.220.jar" org.h2.tools.Server`

2. **Open Browser** (should open automatically at http://localhost:8082)

3. **Connect to Database:**
   - JDBC URL: `jdbc:h2:~/bdms_v2`
   - Username: `sa`
   - Password: (leave blank)
   - Click **Connect**

4. **Run the SQL Script:**
   - Open the file `update-credentials.sql`
   - Copy all the SQL commands
   - Paste into the H2 Console SQL editor
   - Click **Run** (or press Ctrl+Enter)

5. **Verify:**
   - You should see a table showing all updated users
   - Close H2 Console
   - Try logging in with new credentials

---

## 🗑️ Method 3: Delete Database (NUCLEAR OPTION)

**⚠️ WARNING: This will delete ALL your data!**

1. **Close the application** if it's running

2. **Delete the database files:**
   - Navigate to: `C:\Users\YourUsername\`
   - Delete these files:
     - `bdms_v2.mv.db`
     - `bdms_v2.trace.db` (if exists)

3. **Restart the application:**
   - Run `./run-quick.bat`
   - The database will be recreated with new credentials

4. **Login with new credentials:**
   - `superadmin` / `admin123`

---

## 📋 Quick Reference - New Credentials

| Username | Password | Role |
|----------|----------|------|
| superadmin | admin123 | Super Admin |
| owner | owner123 | Owner |
| captain | captain123 | Barangay Captain |
| secretary | secretary123 | Barangay Secretary |
| treasurer | treasurer123 | Barangay Treasurer |
| kagawad | kagawad123 | Kagawads |
| healthworker | health123 | Barangay Health Workers |
| tanod | tanod123 | Barangay Tanods |

---

## 🔍 Troubleshooting

### Still can't login after updating?

1. **Check if database is locked:**
   - Close all instances of the application
   - Wait 10 seconds
   - Try again

2. **Verify credentials were updated:**
   - Use H2 Console (Method 2)
   - Run: `SELECT username, password, role FROM users;`
   - Check if passwords match the new ones

3. **Check for typos:**
   - Usernames and passwords are case-sensitive
   - Make sure there are no extra spaces

4. **Try Super Admin first:**
   - Username: `superadmin`
   - Password: `admin123`
   - This account should always work

### Database file location:

Windows: `C:\Users\YourUsername\bdms_v2.mv.db`

---

## 💡 Why This Happened

The new credentials are only inserted when the database is **empty**. If you had an existing database with old credentials (`pass` for all users), those old credentials remained in place.

The scripts above update the existing database to use the new, more secure credentials.

---

## ✅ After Fixing

Once you can login successfully:

1. **Change your password** (especially for admin accounts)
2. **Review user permissions** in User & Access → Role Permissions
3. **Create additional users** as needed for your barangay
4. **Delete or secure** the credential update scripts

---

**Need Help?**

If none of these methods work, please provide:
- The exact error message you're seeing
- Which username/password you're trying
- Whether you can access H2 Console

For detailed credentials and permissions, see: `LOGIN_CREDENTIALS.md`
