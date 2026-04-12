# Barangay Data Management System (BDMS)

> A comprehensive Java-based desktop application for managing barangay (Filipino administrative division) operations with role-based access control, resident management, document issuance, complaint tracking, and announcements.

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Installation & Setup](#installation--setup)
- [Usage Guide](#usage-guide)
- [Database Schema](#database-schema)
- [User Roles & Access Control](#user-roles--access-control)
- [System Workflows](#system-workflows)
- [Build & Deployment](#build--deployment)
- [Troubleshooting](#troubleshooting)
- [Documentation](#documentation)

---

## 📌 Overview

**BDMS** is a production-ready desktop application built with **Java 21** and **JavaFX** that streamlines barangay administrative operations. The system features:

- **Multi-tab intuitive interface** with light/dark theme support
- **Role-based access control** for different administrative levels
- **Comprehensive data management** for residents, complaints, and documents
- **Audit trail logging** for compliance and accountability
- **PDF report generation** with QR codes for document verification
- **Thread-safe database operations** using embedded H2 database

| Property | Value |
|----------|-------|
| **Project** | java-fx-bdms |
| **Version** | 1.0-SNAPSHOT |
| **Status** | Production Ready |
| **Java** | 21 (LTS) |
| **Build Tool** | Maven 3.11.0+ |

---

## 🎯 Key Features

### ✅ Implemented Features

- ✅ **Multi-tab JavaFX GUI** - Modern, responsive user interface with tabbed navigation
- ✅ **Light/Dark Theme Switching** - Persistent theme preference saved to database
- ✅ **Role-Based Access Control (RBAC)** - 5 user roles with granular permissions
- ✅ **Resident Management** - Full CRUD operations with photo support
- ✅ **Complaint Tracking** - Submit, assign, update status, and generate reports
- ✅ **Announcements Portal** - Create, manage, and track announcement views
- ✅ **Certificate/Document Requests** - Request and issue official documents
- ✅ **PDF Report Generation** - Professional reports with signatures and QR codes
- ✅ **QR Code Generation** - For document verification and traceability
- ✅ **Webcam Integration** - ID photo capture for residents
- ✅ **Audit Trail Logging** - Complete operation history for compliance
- ✅ **Animation & Transitions** - Smooth, professional UX
- ✅ **Thread-Safe Operations** - Platform.runLater() for UI thread safety
- ✅ **H2 Embedded Database** - Zero-config, self-contained data persistence

### 🔄 Future Enhancements

- [ ] Excel export for financial reports
- [ ] SMS notifications for complaint updates
- [ ] Email integration for notifications
- [ ] Mobile app companion
- [ ] Cloud backup & sync capabilities
- [ ] Advanced analytics dashboard
- [ ] Barcode scanning for inventory
- [ ] Multi-language support
- [ ] 2FA authentication
- [ ] Third-party API integration

---

## 🏗️ System Architecture

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                   BDMS APPLICATION LAYER                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  JavaFX GUI (Multi-Tab Interface with Theme Support)     │   │
│  │  ├─ Overview Dashboard     ├─ Resident Management        │   │
│  │  ├─ Certificates/Documents ├─ Complaints & Incidents    │   │
│  │  ├─ Announcements Portal   └─ Security Features (RBAC)   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                               ↓                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │       DATABASE ACCESS LAYER (DatabaseHelper.java)        │   │
│  │  ├─ Connection Management  ├─ Table Initialization       │   │
│  │  ├─ CRUD Operations        └─ Audit Logging (Thread-Safe)│   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│        PERSISTENCE LAYER (H2 Embedded Database)                  │
│  ├─ Location: ~/bdms_v2                                         │
│  ├─ Auto-increment Primary Keys                                 │
│  ├─ Foreign Key Constraints Enabled                             │
│  └─ Audit Trail with Timestamps                                │
└─────────────────────────────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│              FILE SYSTEM & UTILITIES                             │
│  ├─ PDF Report Generation (OpenPDF)                             │
│  ├─ QR Code & Barcode Generation (ZXing)                        │
│  ├─ Photo Storage & Management                                  │
│  ├─ Webcam Integration                                          │
│  └─ CSV/Excel Export Infrastructure                             │
└─────────────────────────────────────────────────────────────────┘
```

### Core Components

| Component | Purpose | Key Classes |
|-----------|---------|------------|
| **Presentation Layer** | User Interface & Interactions | `App.java`, `Launcher.java` |
| **Business Logic** | Data Entities & Operations | `Resident.java`, `Complaint.java`, `Announcement.java`, `DocumentRequest.java` |
| **Persistence Layer** | Database Operations | `DatabaseHelper.java` |
| **Security Layer** | Authentication & Authorization | `User.java`, `Role.java`, `AuditEntry.java` |
| **Utilities** | PDF, QR, Webcam, Reporting | Various utility classes |

---

## 🛠️ Technology Stack

### Core Technologies

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 21 (LTS) |
| **Build Tool** | Apache Maven | 3.11.0+ |
| **GUI Framework** | JavaFX | 21.0.1 |
| **Database** | H2 Database | 2.1.214 |
| **PDF Generation** | OpenPDF | 1.3.30 |
| **QR/Barcode** | ZXing | 3.5.3 |

### Dependencies

| Library | Purpose | Version |
|---------|---------|---------|
| **javafx-controls** | UI controls (buttons, tables, etc.) | 21.0.1 |
| **javafx-graphics** | Rendering engine | 21.0.1 |
| **javafx-fxml** | XML-based UI markup | 21.0.1 |
| **javafx-swing** | Swing integration | 21.0.1 |
| **h2** | Embedded SQL database | 2.1.214 |
| **ikonli-core** | Vector icon framework | 12.3.1 |
| **ikonli-fontawesome5** | 5000+ professional icons | 12.3.1 |
| **zxing-core** | QR/barcode generation | 3.5.3 |
| **zxing-javase** | QR/barcode UI components | 3.5.3 |
| **webcam-capture** | Webcam integration | 0.3.12 |
| **openpdf** | PDF report generation | 1.3.30 |
| **slf4j-api** | Logging framework | 1.7.36 |

### Build Plugins

| Plugin | Version | Purpose |
|--------|---------|---------|
| **maven-compiler-plugin** | 3.11.0 | Java 21 compilation |
| **exec-maven-plugin** | 3.1.0 | Application execution |

---

## 📥 Installation & Setup

### Prerequisites

- **Java 21 JDK** - [Download from Eclipse Adoptium](https://adoptium.net/) or [Microsoft](https://www.microsoft.com/openjdk)
- **Apache Maven 3.9+** - [Download from Apache Maven](https://maven.apache.org/)
- **Windows/Linux/macOS** - JavaFX supports all platforms
- **4GB RAM minimum** recommended
- **Write permissions** in user home directory

### Step-by-Step Installation

#### 1. Clone or Download Project

```bash
git clone <repository-url>
cd Java-FX-BDMS
```

#### 2. Verify Java and Maven

```bash
java -version      # Should show Java 21
mvn -version       # Should show Apache Maven 3.9+
```

#### 3. Install Dependencies

```bash
mvn clean install
```

This command will:
- Download all dependencies
- Compile the project
- Run any configured tests
- Prepare the application for execution

#### 4. Run the Application

**Option A: Using Maven exec plugin (Recommended)**
```bash
mvn exec:java -Dexec.mainClass="com.example.Launcher"
```

**Option B: Using compiled JAR (after Maven build)**
```bash
java -jar target/javafx-app-1.0-SNAPSHOT.jar
```

**Option C: From VS Code**
- Press `Ctrl+Shift+B` → Select "Run BDMS Application"

#### 5. Verify Installation

Upon successful startup:
- ✓ JavaFX window launches with BDMS interface
- ✓ H2 database initializes at `~/bdms_v2`
- ✓ Login screen appears with default credentials
- ✓ No errors in console output

### First-Time Setup

The application will automatically:
1. Create H2 database at user home directory
2. Initialize all required tables
3. Insert default user roles and accounts
4. Log initialization events to audit trail

---

## 💻 Usage Guide

### Default Login Credentials

```
Username: superadmin
Password: pass
Role: Super Admin
```

**Other Available Accounts:**
- `owner` / `pass` (Owner role)
- `secretary` / `pass` (Secretary role)
- `treasurer` / `pass` (Treasurer role)
- `captain` / `pass` (Captain role)
- `resident` / `pass` (Resident role)

### Main Navigation

The application interface consists of:

**Left Sidebar Menu:**
- 🏠 **Overview** - Dashboard with statistics and quick actions
- 👥 **Resident Management** - CRUD operations for residents
- 📄 **Certificates/Documents** - Issue and track document requests
- 🗣️ **Complaints & Incidents** - Track and manage resident complaints
- 📢 **Announcements** - Post and manage announcements
- 📊 **Financial Reports** - View and export financial data
- 🔐 **Security Features** - Audit logs and role management (Admin only)

**Top Navigation Bar:**
- Theme Toggle (Light/Dark mode)
- User Profile & Settings
- Logout

### Common Tasks

#### Add a New Resident

1. Click **Resident** tab in sidebar
2. Click **Add New Resident** button
3. Fill in the form:
   - First Name, Middle Name, Last Name (required)
   - Birth Date (optional)
   - Gender and Civil Status
   - Complete address information
   - Contact information (phone, email)
   - Upload photo (optional)
4. Click **Save**
5. Resident instantly appears in the residents table

#### Submit a Complaint

1. Click **Complaints & Incidents** tab
2. Click **Submit New Complaint** button
3. Complete the form:
   - Title (required)
   - Detailed description (required)
   - Category/Type (optional)
   - Attach photo evidence (optional)
4. Click **Submit**
5. Confirmation notification appears
6. Complaint visible in admin panel with status "Pending"

#### Generate and Issue a Certificate

1. Navigate to **Certificates/Documents** tab
2. Review pending document requests
3. Select a request from the table
4. Click **Generate** button
5. System creates PDF with:
   - Resident information
   - Barangay details and signatures
   - QR code for verification
   - Document seal/stamp
6. Download saves to user's Downloads folder
7. Status automatically updates to "Issued"

#### Create an Announcement

1. Go to **Announcements** tab
2. Click **Create New Announcement**
3. Fill in announcement details:
   - Title (required)
   - Type: Event/Emergency Alert/Program
   - Full content (required, supports rich text)
   - Start Date (required)
   - End Date (optional)
4. Click **Post Announcement**
5. Announcement instantly appears in public announcements feed

---

## 📊 Database Schema

The H2 database stores all application data in 7 core tables:

### Table: `users`

Stores user account information and authentication.

```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL
);
```

**Sample Records:**
- Super Admin account with full system access
- Secretary account for document operations
- Treasurer account for financial management

### Table: `residents`

Stores resident profile information.

```sql
CREATE TABLE residents (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    birth_date VARCHAR(20),
    gender VARCHAR(10),
    address VARCHAR(500),
    image_path VARCHAR(500),
    role VARCHAR(50)
);
```

**Columns:**
- `id` - Unique resident identifier
- `first_name`, `middle_name`, `last_name` - Resident names
- `birth_date` - Date of birth in YYYY-MM-DD format
- `gender` - Male/Female
- `address` - Complete barangay address
- `image_path` - Path to resident photo/ID
- `role` - Resident classification

### Table: `complaints`

Tracks resident complaints and incidents.

```sql
CREATE TABLE complaints (
    id INT PRIMARY KEY AUTO_INCREMENT,
    resident_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    photo_path VARCHAR(500),
    date_submitted DATETIME,
    status VARCHAR(50) DEFAULT 'Pending',
    assigned_to VARCHAR(100),
    notes TEXT,
    FOREIGN KEY (resident_id) REFERENCES residents(id)
);
```

**Fields:**
- `status` - Pending → Ongoing → Resolved
- `assigned_to` - Officer responsible for investigation
- `photo_path` - Evidence photos
- `notes` - Internal investigation notes

### Table: `announcements`

Public announcements and notices.

```sql
CREATE TABLE announcements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    content TEXT,
    date_posted DATETIME,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'Active',
    views INT DEFAULT 0
);
```

**Types:**
- Event - Community events and programs
- Emergency Alert - Safety notifications
- Program - Government initiatives

### Table: `document_requests`

Requests for official certificates and documents.

```sql
CREATE TABLE document_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    resident_id INT NOT NULL,
    document_type VARCHAR(100),
    request_date DATETIME,
    status VARCHAR(50) DEFAULT 'Pending',
    notes TEXT,
    FOREIGN KEY (resident_id) REFERENCES residents(id)
);
```

**Document Types:**
- Barangay Clearance
- Residency Certificate
- Indigency Certificate
- Business Permit Endorsement
- Custom Documents

### Table: `roles`

System roles and permission configurations.

```sql
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(100) UNIQUE,
    permissions TEXT,
    description VARCHAR(500)
);
```

**Default Roles:**
- Super Admin - Full system access
- Secretary - Document and resident management
- Treasurer - Financial operations
- Captain - Administrative oversight
- Resident - Limited public access

### Table: `audit_log`

Complete audit trail of all system operations.

```sql
CREATE TABLE audit_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    timestamp VARCHAR(30) NOT NULL,
    username VARCHAR(50) DEFAULT 'System',
    action VARCHAR(200) NOT NULL,
    details VARCHAR(500),
    category VARCHAR(50)
);
```

**Actions Logged:**
- CREATE - New records added
- READ - Data Access
- UPDATE - Record modifications
- DELETE - Record removal
- LOGIN/LOGOUT - User sessions
- APPROVE/DENY - Administrative decisions

**Categories:**
- Resident - Resident operations
- Document - Certificate/document operations
- Complaint - Complaint handling
- Announcement - Announcements
- Security - User management and access
- Payment - Financial transactions

---

## 👥 User Roles & Access Control

### Role Hierarchy

| Role | Description | Permissions |
|------|-------------|-----------|
| **Super Admin** | Full system access | ✅ All features, user management, settings |
| **Secretary** | Operations manager | ✅ Residents, Documents, Complaints, Announcements |
| **Treasurer** | Financial management | ✅ Financial reports, Collections, Budget |
| **Captain** | Administrative oversight | ✅ All read access, Announcements, Audit logs |
| **Resident** | Limited public access | ✅ Submit complaints, View announcements, Request documents |

### Permission Matrix

| Feature | Super Admin | Secretary | Treasurer | Captain | Resident |
|---------|:-:|:-:|:-:|:-:|:-:|
| View Residents | ✔️ | ✔️ | ✔️ | ✔️ | ✖️ |
| Manage Residents | ✔️ | ✔️ | ✖️ | ✖️ | ✖️ |
| Issue Documents | ✔️ | ✔️ | ✖️ | ✖️ | ✖️ |
| View Complaints | ✔️ | ✔️ | ✖️ | ✔️ | ✖️ |
| Manage Complaints | ✔️ | ✔️ | ✖️ | ✖️ | ✖️ |
| Financial Reports | ✔️ | ✖️ | ✔️ | ✔️ | ✖️ |
| Manage Users | ✔️ | ✖️ | ✖️ | ✖️ | ✖️ |
| Audit Logs | ✔️ | ✖️ | ✖️ | ✔️ | ✖️ |
| Submit Complaint | ✔️ | ✔️ | ✔️ | ✔️ | ✔️ |
| View Announcements | ✔️ | ✔️ | ✔️ | ✔️ | ✔️ |

---

## 🔄 System Workflows

### Application Startup Flow

```
User Launches BDMS
    ↓
Launcher.java executes
    ↓
App.java initializes (extends Application)
    ↓
DatabaseHelper.initializeDatabase()
    ├─ Load H2 JDBC Driver
    ├─ Create connection to ~/bdms_v2
    ├─ Create tables (if not exists):
    │  ├─ users
    │  ├─ residents
    │  ├─ complaints
    │  ├─ announcements
    │  ├─ document_requests
    │  ├─ roles
    │  └─ audit_log
    ├─ Insert default roles and users
    └─ Log initialization to audit_log
    ↓
Load JavaFX Scene
    ├─ Apply saved theme (Light/Dark)
    ├─ Initialize navigation menu
    ├─ Load login dialog
    └─ Ready for user authentication
```

### User Authentication Flow

```
User enters credentials
    ↓
Click Login
    ↓
Query users table for username
    ↓
Verify password match
    ↓
Fetch user role and permissions
    ↓
Load Dashboard
    ├─ Display accessible tabs based on role
    ├─ Load relevant data
    └─ Apply role-specific UI restrictions
    ↓
Log login event to audit_log
    ↓
Application ready for use
```

### Resident Management Workflow

```
Admin opens Resident tab
    ↓
DatabaseHelper.getAllResidents()
    ↓
Execute: SELECT * FROM residents
    ↓
Display results in TableView
    ↓
User Actions:
    ├─ CREATE: Fill form → INSERT into DB
    ├─ READ: Select row → View Details Dialog
    ├─ UPDATE: Edit form → UPDATE in DB
    └─ DELETE: Confirm → DELETE from DB
    ↓
Database operation complete
    ↓
Log action to audit_log
    ↓
Refresh table (Thread-safe with Platform.runLater())
```

### Document Request & Issuance

```
Resident requests certificate
    ↓
Select document type
    ├─ Barangay Clearance
    ├─ Residency Certificate
    ├─ Indigency Certificate
    └─ Other
    ↓
Submit request
    ↓
INSERT into document_requests (status='Pending')
    ↓
Secretary reviews request
    ↓
Click Generate Certificate
    ↓
System creates PDF:
    ├─ Fetch resident data
    ├─ Apply official template
    ├─ Add signatures and seal
    ├─ Generate QR code
    └─ Save to ~/Downloads/
    ↓
Update status to 'Issued'
    ↓
Log to audit_log
```

---

## 📝 Build & Deployment

### Development Build

```bash
# Clean and compile
mvn clean compile

# Run directly from Maven
mvn exec:java -Dexec.mainClass="com.example.Launcher"
```

### Production Build

```bash
# Build executable JAR
mvn clean package

# Run the JAR file
java -jar target/javafx-app-1.0-SNAPSHOT.jar
```

### Build Commands Summary

| Command | Purpose |
|---------|---------|
| `mvn clean` | Remove build artifacts |
| `mvn compile` | Compile source code |
| `mvn test` | Run unit tests |
| `mvn package` | Create executable JAR |
| `mvn install` | Install dependencies |
| `mvn exec:java` | Run application directly |
| `mvn dependency:tree` | View dependency hierarchy |

---

## 🐛 Troubleshooting

### Issue: "No suitable driver found" for H2

**Cause:** H2 JDBC driver not in classpath

**Solution:**
```bash
# Verify h2 dependency in pom.xml (should be 2.1.214)
# Reinstall dependencies:
mvn clean install

# Verify maven-compiler-plugin version 3.11.0+
```

### Issue: Database file not found (~/bdms_v2)

**Cause:** First-time launch or permission issues

**Solution:**
```bash
# This is normal on first launch - the app creates it automatically
# Verify write permissions in home directory:
ls -la ~  # Linux/Mac
dir %USERPROFILE%  # Windows

# Check if database was created:
ls -la ~/bdms_v2*  # Linux/Mac
dir %USERPROFILE%\bdms_v2*  # Windows
```

### Issue: JavaFX modules not loading

**Cause:** JavaFX SDK not properly configured

**Solution:**
```bash
# Verify Java 21 is installed:
java -version

# If using IDE, add VM arguments:
--module-path /path/to/javafx-sdk-21.0.1/lib
--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing
```

### Issue: Application crashes on startup

**Cause:** Missing dependencies or compilation errors

**Solution:**
```bash
# Check for errors:
mvn clean install

# View dependency tree:
mvn dependency:tree

# Rebuild from scratch:
mvn clean
mvn install
mvn exec:java -Dexec.mainClass="com.example.Launcher"
```

### Issue: Theme not persisting after restart

**Cause:** Theme preference not saved to database

**Solution:**
```bash
# Verify database exists:
ls -la ~/bdms_v2*

# Check database connection in DatabaseHelper.java
# Ensure JDBC URL points to correct location
# Restart application
```

### Issue: Data not saving to database

**Cause:** Connection issues or permissions

**Solution:**
```bash
# Check H2 database connection:
# - Verify JDBC URL: jdbc:h2:~/bdms_v2
# - Verify username: SA (default)
# - Check disk space in home directory
# - Review audit_log for error entries
# - Check DatabaseHelper logs
```

---

## 📁 File Structure

```
Java-FX-BDMS/
├── pom.xml                           # Maven configuration (dependencies, plugins, build)
├── README.md                         # This documentation file
├── DATABASE_SCHEMA.md                # Detailed database schema documentation
├── PRODUCTION_EVALUATION.md          # Testing checklist and validation
├── INTEGRATION_READINESS_REPORT.md   # Integration testing status
├── TESTING_LOG.md                    # Test execution logs and results
├── bdms_dump.sql                     # Database backup/export file
│
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── Launcher.java         # Application entry point
│   │   │   ├── App.java              # Main JavaFX application class
│   │   │   ├── DatabaseHelper.java   # Database operations and CRUD
│   │   │   ├── Resident.java         # Resident entity model
│   │   │   ├── Complaint.java        # Complaint entity model
│   │   │   ├── Announcement.java     # Announcement entity model
│   │   │   ├── DocumentRequest.java  # Document request entity model
│   │   │   ├── User.java             # User authentication entity
│   │   │   ├── Role.java             # Role and permissions entity
│   │   │   ├── AuditEntry.java       # Audit log entry entity
│   │   │   ├── light-theme.css       # Light theme stylesheet
│   │   │   └── dark-theme.css        # Dark theme stylesheet
│   │   │
│   │   └── resources/
│   │       └── (configuration files)
│   │
│   └── assets/
│       ├── images/
│       ├── icons/
│       └── (other resources)
│
├── target/
│   ├── classes/                      # Compiled .class files
│   ├── javafx-app-1.0-SNAPSHOT.jar   # Executable application JAR
│   └── (other build artifacts)
│
└── .github/
    └── copilot-instructions.md       # VS Code Copilot configuration
```

### Key Java Classes

| Class | Purpose | Key Methods |
|-------|---------|------------|
| `Launcher.java` | Application entry point | `main()` |
| `App.java` | Main JavaFX application | `start()`, `initializeUI()` |
| `DatabaseHelper.java` | All database operations | CRUD methods, Query execution |
| `Resident.java` | Resident data model | Getters/setters, Builder pattern |
| `Complaint.java` | Complaint tracking model | Status management |
| `Announcement.java` | Announcement data model | View counter, Type enum |
| `DocumentRequest.java` | Document request model | Status workflow |
| `AuditEntry.java` | Audit log model | Timeline-based queries |

---

## 📚 Documentation

Comprehensive documentation is available in the following files:

| Document | Content |
|----------|---------|
| [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) | Complete database table definitions and relationships |
| [PRODUCTION_EVALUATION.md](PRODUCTION_EVALUATION.md) | Testing checklist and production readiness |
| [INTEGRATION_READINESS_REPORT.md](INTEGRATION_READINESS_REPORT.md) | System integration status and compatibility |
| [TESTING_LOG.md](TESTING_LOG.md) | Test execution results and coverage reports |

---

## 📞 Support & Contributing

### Reporting Issues

If you encounter issues:

1. Check the [Troubleshooting](#troubleshooting) section
2. Review logs in `audit_log` table
3. Check [TESTING_LOG.md](TESTING_LOG.md) for known issues
4. Verify all prerequisites are installed

### Getting Help

- Review the inline code comments
- Check the system flow diagrams
- Consult the database schema documentation
- Review test cases for usage examples

---

## 📄 License & Credits

**BDMS v1.0**

Built with:
- Java 21 (LTS) - Enterprise stability
- JavaFX 21.0.1 - Modern UI framework
- H2 2.1.214 - Embedded database
- Apache Maven - Build automation
- OpenPDF - Document generation
- ZXing - QR code generation

**Project Status:** ✅ Production Ready

---

## ✨ Quick Reference

### Essential Commands

```bash
# Install and run
mvn clean install && mvn exec:java -Dexec.mainClass="com.example.Launcher"

# Build JAR for distribution
mvn clean package

# Run compiled JAR
java -jar target/javafx-app-1.0-SNAPSHOT.jar

# View dependencies
mvn dependency:tree

# Generate database dump
java -cp ".;target/classes;$MAVEN_HOME/h2-2.1.214.jar" ExportDatabase
```

### Default Credentials

```
Username: superadmin
Password: pass
Role: Super Admin (Full Access)
```

### Database Location

```
Linux/macOS: ~/.bdms_v2
Windows: C:\Users\<YourUsername>\bdms_v2
```

### Important Directories

```
Application: C:\Users\Razza\OneDrive\Desktop\Java-FX-BDMS
Database: ~/bdms_v2
Reports: ~/Downloads/
Logs: Console output & audit_log table
```

---

**Last Updated:** April 12, 2026  
**Java Version:** 21 (LTS)  
**JavaFX Version:** 21.0.1  
**H2 Database:** 2.1.214
# Barangay Data Management System (BDMS)

## 📋 Table of Contents
- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [System Flow & Features](#system-flow---features)
- [Installation & Setup](#installation--setup)
- [Usage Guide](#usage-guide)
- [Database Schema](#database-schema)
- [User Roles & Access Control](#user-roles--access-control)
- [Key Features](#key-features)
- [File Structure](#file-structure)
- [Troubleshooting](#troubleshooting)

---

## 📌 Overview

**Barangay Data Management System (BDMS)** is a comprehensive Java-based desktop application designed to manage and organize data for barangay (Filipino administrative division) operations. The system provides role-based access control, resident management, document issuance, complaint tracking, announcements, and financial reporting—all with an intuitive graphical user interface built with JavaFX.

**Project Name:** java-fx-bdms  
**Version:** 1.0-SNAPSHOT  
**Status:** Production Ready (Multi-Tab Integration Test Phase)

---

## 🏗️ System Architecture

### Architecture Overview
```
┌─────────────────────────────────────────────────────────────────┐
│                       BDMS APPLICATION LAYER                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ JavaFX GUI (Multi-Tab Interface with Theme Support)      │   │
│  │ - Overview Tab                                           │   │
│  │ - Resident Management Tab                                │   │
│  │ - Certificates/Documents Tab                             │   │
│  │ - Complaints & Incidents Tab                             │   │
│  │ - Announcements Portal Tab                               │   │
│  │ - Financial Reports Tab                                  │   │
│  │ - Security Features Tab (RBAC)                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                               ↓                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │           DATABASE ACCESS LAYER (DatabaseHelper)         │   │
│  │ - Connection Management                                  │   │
│  │ - Table Initialization                                   │   │
│  │ - CRUD Operations (Thread-Safe)                          │   │
│  │ - Audit Logging                                          │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│              PERSISTENCE LAYER (H2 Embedded Database)             │
│  └─────────────────────────────────────────────────────────────┘│
│  Database: ~/bdms_v2                                            │
│  - Auto-increment PKs                                           │
│  - Foreign Key Constraints                                      │
│  - Audit Trail Logging                                          │
└─────────────────────────────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│                    FILE SYSTEM & UTILITIES                       │
│  - PDF Report Generation (OpenPDF)                              │
│  - QR Code Generation & Barcode Scanning                        │
│  - Photo Storage & Management                                   │
│  - CSV/Excel Export                                             │
│  - Webcam Integration                                           │
└─────────────────────────────────────────────────────────────────┘
```

### Core Components

| Component | Purpose | Key Classes |
|-----------|---------|------------|
| **Presentation Layer** | User Interface & interactions | `App.java`, `Launcher.java` |
| **Business Logic** | Data entities & operations | `Resident`, `Complaint`, `Announcement`, `DocumentRequest` |
| **Persistence Layer** | Database operations | `DatabaseHelper.java` |
| **Security Layer** | Authentication & Authorization | `User.java`, `Role.java`, `AuditEntry.java` |
| **Utilities** | PDF, QR codes, Webcam | Built-in libraries |

---

## 🛠️ Technology Stack

### Java & Build
- **Java Version:** Java 21 (LTS - Long Term Support)
- **Build Tool:** Apache Maven 3.11.0
- **Build Status:** ✅ SUCCESS (0 errors, 0 warnings)

### GUI Framework
- **JavaFX:** 21.0.1 (Modern, Hardware-Accelerated UI)
  - javafx-controls: UI controls (buttons, tables, etc.)
  - javafx-graphics: Rendering engine
  - javafx-fxml: XML-based UI markup (optional)
  - javafx-swing: Swing integration for legacy components

### Database
- **H2 Database:** 2.1.214 (Embedded, SQL-compliant, zero-config)
  - Location: `~/bdms_v2`
  - Auto-increment Primary Keys
  - Foreign Key Constraints enabled
  - AUTO_SERVER=TRUE for multi-instance support

### Additional Libraries

| Library | Purpose | Version |
|---------|---------|---------|
| **Ikonli** | Vector Icon Framework | 12.3.1 |
| **FontAwesome 5** | Icon Pack (5000+ icons) | 12.3.1 |
| **ZXing** | QR Code & Barcode generation/reading | 3.5.3 |
| **Webcam Capture** | Webcam integration for ID verification | 0.3.12 |
| **OpenPDF** | PDF Report Generation | 1.3.30 |
| **SLF4J** | Logging Framework | 1.7.36 |

### Plugins
- **maven-compiler-plugin:** 3.11.0 (Java 21 compilation)
- **exec-maven-plugin:** 3.1.0 (Application execution)

---

## 🔄 System Flow & Features

### 1. **Application Startup Flow**

```
User Launches BDMS
    ↓
Launcher.java (Main Entry Point)
    ↓
App.java initializes (extends Application)
    ↓
DatabaseHelper.initializeDatabase()
    ├─ Load H2 JDBC Driver
    ├─ Establish Connection (~/bdms_v2)
    ├─ Create Tables (if not exists)
    │   ├─ users
    │   ├─ residents
    │   ├─ complaints
    │   ├─ announcements
    │   ├─ document_requests
    │   ├─ roles
    │   └─ audit_log
    ├─ Insert Default Roles (Super Admin, Secretary, etc.)
    └─ Log to audit_log table
    ↓
Load JavaFX Scene (Multi-Tab Interface)
    ├─ Apply Theme (Light/Dark CSS)
    ├─ Initialize Navigation Sidebar
    ├─ Load Overview Dashboard
    └─ Ready for User Interaction
```

### 2. **User Authentication & Authorization**

```
Login Attempt
    ↓
Query users table (username verification)
    ↓
Password validation (plaintext in DB * see security notes)
    ↓
Fetch user role → Query roles table
    ↓
Load Role Permissions
    ├─ View Resident Data
    ├─ Manage Financials
    ├─ Access Blotter/Legal
    ├─ System Settings
    └─ Generate Reports
    ↓
Load Dashboard (with role-specific UI)
    ├─ Super Admin → All features visible
    ├─ Secretary → Resident & Document tabs active
    ├─ Treasurer → Financial Reports tab active
    ├─ Barangay Captain → Admin oversight
    └─ Resident → Limited view (submit complaints only)
    ↓
Log login event → audit_log table
```

### 3. **Resident Management Flow**

```
Administrator Opens "Resident" Tab
    ↓
DatabaseHelper.getAllResidents()
    ↓
Execute: SELECT * FROM residents
    ↓
Return ObservableList<Resident>
    ↓
Populate TableView with:
    ├─ First Name
    ├─ Middle Name
    ├─ Last Name
    ├─ Birth Date
    ├─ Gender
    ├─ Address
    └─ Action Buttons (View, Edit, Delete)
    ↓
User Action:
    ├─ CREATE: Fill form → INSERT into residents
    ├─ READ: Select row → View Details Dialog
    ├─ UPDATE: Fill form → UPDATE residents table
    └─ DELETE: Confirm → DELETE from residents
    ↓
Log operation → audit_log table
    ↓
Refresh table view (Thread-safe with Platform.runLater())
```

### 4. **Complaint Management Flow**

```
Resident Submits Complaint
    ↓
Fill Form:
    ├─ Title (required)
    ├─ Description (required)
    ├─ Category (optional, ComboBox)
    ├─ Photo Upload (optional, FileChooser)
    └─ Submit Button
    ↓
Validation:
    ├─ Verify required fields filled
    ├─ Optional: Compress photo
    └─ Set timestamp & status=Pending
    ↓
INSERT into complaints table:
    ├─ residentID
    ├─ title
    ├─ description
    ├─ photo_path
    ├─ date_submitted
    ├─ status (Pending)
    └─ assigned_to (null initially)
    ↓
Admin Views Complaints Tab
    ↓
Query complaints table → Display in table
    ↓
Admin Actions:
    ├─ View Details (Dialog shows full info + photo)
    ├─ Update Status (Pending → Ongoing → Resolved)
    ├─ Assign To (Secretary/Officer)
    ├─ Add Notes (Internal comments)
    └─ Generate PDF Report (Download to ~/Downloads/)
    ↓
All changes → audit_log table
```

### 5. **Announcements Portal Flow**

```
Admin Creates Announcement
    ↓
Fill Form:
    ├─ Title (required)
    ├─ Type (ComboBox: Event/Emergency Alert/Program)
    ├─ Content (required, RichTextArea)
    ├─ Start Date (required, DatePicker)
    └─ End Date (optional)
    ↓
INSERT into announcements table:
    ├─ title
    ├─ type
    ├─ content
    ├─ date_posted (automatic)
    ├─ start_date
    ├─ end_date
    ├─ status (Active)
    └─ views (0)
    ↓
Residents View Announcements Tab
    ↓
Fetch announcements WHERE status='Active'
    ↓
Display in:
    ├─ Card-based layout (modern UI)
    ├─ Each card shows title, type badge, date
    └─ Click → Expand for full content
    ↓
Views counter incremented on each read
```

### 6. **Document Request & Certification Flow**

```
Resident Requests Certificate
    ↓
Form Options:
    ├─ Barangay Clearance
    ├─ Residency Certificate
    ├─ Indigency Certificate
    ├─ Business Permit Endorsement
    └─ Custom Document
    ↓
INSERT into document_requests table:
    ├─ residentID
    ├─ document_type
    ├─ request_date
    ├─ status (Pending)
    └─ notes
    ↓
Secretary Reviews Requests
    ↓
Query document_requests WHERE status='Pending'
    ↓
Secretary Actions:
    ├─ Review request details
    ├─ Generate Certificate (PDF)
    │   ├─ Fetch resident data
    │   ├─ Apply barangay template
    │   ├─ Add official signatures
    │   ├─ Generate QR code (for verification)
    │   └─ Save as PDF
    ├─ Update status → Issued
    └─ Send notification to resident
    ↓
Resident Downloads Certificate
    ↓
Logs tracked in audit_log
```

### 7. **Financial Reporting Flow**

```
Treasurer Opens "Financial Reports" Tab
    ↓
Query financial data:
    ├─ Collections (if implemented)
    ├─ Expenditures (if implemented)
    └─ Budget Summary
    ↓
Generate Charts:
    ├─ Pie Chart (expense breakdown)
    ├─ Bar Chart (monthly trend)
    └─ Summary Statistics
    ↓
Export Options:
    ├─ PDF Report → ~/Downloads/financial_report_[DATE].pdf
    ├─ Excel Export (future)
    └─ Print (future)
    ↓
All access logged to audit_log
```

### 8. **Security & Audit Trail Flow**

```
Every System Operation
    ↓
Capture Event:
    ├─ Timestamp (automatic)
    ├─ Username (logged-in user)
    ├─ Action (CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT)
    ├─ Category (Resident, Complaint, Document, etc.)
    └─ Details (operation summary)
    ↓
INSERT into audit_log table
    ↓
Admin Reviews Security Tab
    ↓
Query audit_log for:
    ├─ All users' recent activities
    ├─ Filtered by action type
    ├─ Filtered by date range
    └─ Filtered by category
    ↓
Display Audit Trail Table:
    ├─ Timestamp
    ├─ Username
    ├─ Action
    ├─ Details
    └─ Category
    ↓
Compliance & Accountability verified
```

---

## 📥 Installation & Setup

### Prerequisites
- **Java 21 JDK** (must be installed)
- **Apache Maven 3.11.0+** (or let IDE detect)
- **Windows/Linux/Mac** (JavaFX supports all platforms)
- **4GB RAM minimum** recommended

### Step 1: Clone/Download Project
```bash
git clone <repository-url>
cd Java-FX-BDMS
```

### Step 2: Verify Java & Maven
```bash
java -version  # Should show java 21
mvn -version   # Should show Apache Maven 3.11.0+
```

### Step 3: Clean Build
```bash
mvn clean install
```

### Step 4: Run Application
```bash
# Option 1: Maven exec plugin
mvn exec:java -Dexec.mainClass="com.example.Launcher"

# Option 2: Run JAR (if compiled)
java -jar target/javafx-app-1.0-SNAPSHOT.jar

# Option 3: VS Code (if tasks configured)
Press Ctrl+Shift+B → Select "Run BDMS Application"
```

### Step 5: Verify Startup
- JavaFX window launches with BDMS logo
- Database initialized at `~/bdms_v2`
- Login screen appears

---

## 💻 Usage Guide

### Login Credentials (Default)
```
Username: admin
Password: admin123
Role: Super Admin
```

### Navigation

1. **Sidebar Menu** (Left side)
   - Overview (Dashboard)
   - Resident (Manage residents)
   - Certificates (Issue documents)
   - Complaints (Track incidents)
   - Announcements (Post notices)
   - Financial Reports
   - Security Features (Admin only)

2. **Theme Toggle** (Top-right corner)
   - Light Theme: Professional, clean appearance
   - Dark Theme: Reduced eye strain, modern aesthetic

3. **User Account** (Top-right)
   - View Profile
   - Change Password
   - Logout

### Feature Usage Examples

#### Add a New Resident
1. Click "Resident" tab
2. Click "Add New Resident" button
3. Fill form:
   - First Name, Middle Name, Last Name (required)
   - Birth Date (optional)
   - Gender, Civil Status
   - Address information
   - Contact info (phone, email)
4. Click "Save"
5. Resident appears in table immediately

#### Submit a Complaint
1. Click "Complaints" tab
2. Click "Submit New Complaint"
3. Fill form:
   - Title (required)
   - Description (required)
   - Attach photo (optional)
4. Click "Submit"
5. Notification confirms submission
6. Complaint visible in admin panel

#### Generate Certificate
1. Click "Certificates" tab
2. Review pending requests
3. Select request → Click "Generate"
4. System creates PDF with resident info, signatures, QR code
5. Download saves to ~/Downloads/
6. Status updated to "Issued"

---

## 📊 Database Schema

The H2 database (`~/bdms_v2`) contains 7 core tables:

### Table: residents
```sql
Columns:
- id (INT, PK, AUTO_INCREMENT)
- first_name (VARCHAR 100, NOT NULL)
- middle_name (VARCHAR 100)
- last_name (VARCHAR 100, NOT NULL)
- birth_date (VARCHAR 20)
- gender (VARCHAR 10)
- address (VARCHAR 500)
- image_path (VARCHAR 500)
- role (VARCHAR 50)
```

### Table: users
```sql
Columns:
- id (INT, PK, AUTO_INCREMENT)
- username (VARCHAR 50, UNIQUE, NOT NULL)
- password (VARCHAR 50, NOT NULL)
- role (VARCHAR 20, NOT NULL)
```

### Table: complaints
```sql
Columns:
- id (INT, PK, AUTO_INCREMENT)
- resident_id (INT, FK → residents.id)
- title (VARCHAR 255, NOT NULL)
- description (TEXT)
- photo_path (VARCHAR 500)
- date_submitted (DATETIME)
- status (VARCHAR 50, DEFAULT 'Pending')
- assigned_to (VARCHAR 100)
- notes (TEXT)
```

### Table: announcements
```sql
Columns:
- id (INT, PK, AUTO_INCREMENT)
- title (VARCHAR 255, NOT NULL)
- type (VARCHAR 50) -- Event, Emergency Alert, Program
- content (TEXT)
- date_posted (DATETIME)
- start_date (DATE)
- end_date (DATE)
- status (VARCHAR 20, DEFAULT 'Active')
- views (INT, DEFAULT 0)
```

### Table: document_requests
```sql
Columns:
- id (INT, PK, AUTO_INCREMENT)
- resident_id (INT, FK → residents.id)
- document_type (VARCHAR 100)
- request_date (DATETIME)
- status (VARCHAR 50, DEFAULT 'Pending')
- notes (TEXT)
```

### Table: roles
```sql
Columns:
- id (INT, PK, AUTO_INCREMENT)
- role_name (VARCHAR 100, UNIQUE)
- permissions (TEXT) -- JSON or comma-separated
- description (VARCHAR 500)
```

### Table: audit_log
```sql
Columns:
- id (INT, PK, AUTO_INCREMENT)
- timestamp (VARCHAR 30)
- username (VARCHAR 50, DEFAULT 'System')
- action (VARCHAR 200) -- CREATE, READ, UPDATE, DELETE, LOGIN
- details (VARCHAR 500)
- category (VARCHAR 50) -- Resident, Complaint, Security, etc.
```

---

## 👥 User Roles & Access Control

### Role Hierarchy

| Role | Description | Permissions |
|------|-------------|-----------|
| **Super Admin** | Full system access | ✅ All features, user management, role management, audit logs |
| **Secretary** | User & document management | ✅ Resident CRUD, Certificate generation, Complaint assignment |
| **Treasurer** | Financial operations | ✅ Financial reports, Collections tracking, Budget review |
| **Barangay Captain** | Administrative oversight | ✅ All tabs (read), Announcements (create), Audit logs (read) |
| **Resident** | Limited user access | ✅ Submit complaints, View announcements, Request certificates |

### Permission Granularity
Each role has permissions across these categories:
- **Resident Data:** Create, Read, Update, Delete
- **Financials:** Manage, Reports, Audit
- **Blotter/Legal:** View, Manage, Investigate
- **System Settings:** User management, Roles, Audit logs
- **Documents:** Generate, Verify, Archive

---

## 🎯 Key Features

### ✅ Implemented Features
- ✅ Multi-tab JavaFX GUI with modern UI
- ✅ Light/Dark theme switching (persistent)
- ✅ Role-based access control (RBAC)
- ✅ Resident management (CRUD)
- ✅ Complaint tracking with status updates
- ✅ Announcements portal
- ✅ Certificate/Document request system
- ✅ PDF report generation
- ✅ QR code generation (for certificates)
- ✅ Webcam integration (ID capture)
- ✅ Audit trail logging (all operations)
- ✅ Animation & transitions (smooth UX)
- ✅ Thread-safe database operations (Platform.runLater)
- ✅ H2 embedded database (zero-config)

### 🔄 Future Enhancements
- [ ] Excel export (financial reports)
- [ ] SMS notifications (complaint updates)
- [ ] Email integration
- [ ] Mobile app companion
- [ ] Cloud backup & sync
- [ ] Advanced analytics dashboard
- [ ] Barcode scanning (inventory)
- [ ] Multi-language support
- [ ] 2FA authentication (security)
- [ ] API for third-party integration

---

## 📁 File Structure

```
Java-FX-BDMS/
├── pom.xml                              # Maven configuration (Java 21, dependencies)
├── README.md                            # This file
├── DATABASE_SCHEMA.md                   # Detailed schema documentation
├── PRODUCTION_EVALUATION.md             # Testing checklist
├── INTEGRATION_READINESS_REPORT.md      # Integration status
├── TESTING_LOG.md                       # Test execution log
│
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── Launcher.java            # Application entry point
│   │   │   ├── App.java                 # Main JavaFX application (extends Application)
│   │   │   ├── DatabaseHelper.java      # Database connection & CRUD operations
│   │   │   ├── Resident.java            # Entity: Resident data model
│   │   │   ├── Complaint.java           # Entity: Complaint data model
│   │   │   ├── Announcement.java        # Entity: Announcement data model
│   │   │   ├── DocumentRequest.java     # Entity: Document request model
│   │   │   ├── User.java                # Entity: User authentication
│   │   │   ├── Role.java                # Entity: User role/permissions
│   │   │   ├── AuditEntry.java          # Entity: Audit log entry
│   │   │   ├── light-theme.css          # Light theme stylesheet
│   │   │   └── dark-theme.css           # Dark theme stylesheet
│   │   │
│   │   └── resources/
│   │       └── (configuration files)
│   │
│   └── assets/
│       ├── images/
│       ├── icons/
│       └── (other resources)
│
├── target/
│   ├── classes/                         # Compiled .class files
│   ├── javafx-app-1.0-SNAPSHOT.jar      # Executable JAR
│   └── (build artifacts)
│
└── .github/
    └── copilot-instructions.md          # VS Code Copilot instructions
```

### Key Java Files Explained

- **Launcher.java:** Entry point, launches App
- **App.java:** Main application class, initializes UI, manages tabs
- **DatabaseHelper.java:** Static class handling all database operations, connection pooling, CRUD methods
- **Entity Classes:** Data models (Resident, Complaint, etc.) with getters/setters
- **CSS Files:** Mobile-responsive dark/light themes

---

## 🐛 Troubleshooting

### Issue: "No suitable driver found" for H2
**Solution:**
```
Verify in pom.xml: h2 dependency version 2.1.214
Run: mvn clean install
Ensure: maven-compiler-plugin version 3.11.0 (Java 21 support)
```

### Issue: Database file not found (~/bdms_v2)
**Solution:**
```
Normal behavior on first launch
App auto-creates database
Check: User home directory (~) is accessible
Ensure: Write permissions in home folder
```

### Issue: JavaFX modules not loading
**Solution:**
```
Verify Java 21 installed: java -version
Add JVM arguments if running from IDE:
  --module-path /path/to/javafx-sdk-21.0.1/lib 
  --add-modules javafx.controls,javafx.fxml,javafx.graphics
```

### Issue: Application crashes on startup
**Solution:**
```
1. Check console for errors
2. Verify all dependencies installed: mvn dependency:tree
3. Clear cache: mvn clean
4. Rebuild: mvn install
```

### Issue: Theme not persisting after restart
**Solution:**
```
Check: User preferences file (usually in ~/.bdms/)
Solution: Theme preference saved to database (users table)
Verify: Database not corrupted: Check ~/bdms_v2
```

### Issue: Complaints/Announcements not saving
**Solution:**
```
1. Check database connection: DatabaseHelper logs
2. Verify H2 JDBC URL: settings in DatabaseHelper
3. Check disk space: ~/bdms_v2 directory
4. Review audit_log for error entries
```

---

## 📝 Build & Deployment

### Compile Project
```bash
mvn clean compile
```

### Run Tests (if configured)
```bash
mvn test
```

### Package as JAR
```bash
mvn package
```

### Execute JAR
```bash
java -jar target/javafx-app-1.0-SNAPSHOT.jar
```

### Clean Build Artifacts
```bash
mvn clean
```

---

## 📞 Support & Documentation

For detailed information:
- See [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) for table definitions
- See [PRODUCTION_EVALUATION.md](PRODUCTION_EVALUATION.md) for testing status
- See [TESTING_LOG.md](TESTING_LOG.md) for test results

---

## 📄 License & Credits

**BDMS v1.0**  
Built with Java 21 + JavaFX 21.0.1 + H2 Database

---

## ✨ Quick Start Commands

```bash
# Install dependencies
mvn clean install

# Run application
mvn exec:java -Dexec.mainClass="com.example.Launcher"

# View dependency tree
mvn dependency:tree

# Build JAR
mvn package

# Run compiled JAR
java -jar target/javafx-app-1.0-SNAPSHOT.jar
```

---

**Last Updated:** April 10, 2026  
**For Questions:** Review DATABASE_SCHEMA.md or PRODUCTION_EVALUATION.mdFX 21 desktop application for managing barangay data, built with Maven and H2 database.

## Features

- User authentication with role-based permissions
- Dashboard with analytics and overview
- UptimeRobot-inspired login UI design
- H2 embedded database for data persistence

## Prerequisites

- JDK 21 (installed locally)
- Maven 3.9+

## Running the Application

### Using Maven (Recommended)

```bash
mvn clean compile exec:java
```

This will compile and run the application with proper JavaFX classpath.

### VS Code Development

- **Editing**: Use VS Code for code editing and navigation
- **Running**: Use terminal with `mvn exec:java` (VS Code debug has classpath issues with JavaFX)
- **Debugging**: For debugging, run `mvn exec:java` in terminal, then attach debugger from VS Code using the "Debug Maven Exec" configuration

## Project Structure

- `src/main/java/com/example/App.java` - Main application class
- `src/main/java/com/example/DatabaseHelper.java` - Database operations
- `pom.xml` - Maven configuration with JavaFX dependencies

## Database

Uses H2 embedded database with sample users:
- **superadmin** / **pass** (Super Admin role)
- **secretary** / **pass** (Secretary role)
- And others (captain, treasurer, etc.)

## Technologies

- Java 21
- JavaFX 21
- Maven
- H2 Database#   B a r a n g a y - S a n - M a r i n o - I n f o r m a t i o n - M a n a g e m e n t - S y s t e m - 
 
 