# BDMS Database Schema Documentation

**Date:** April 9, 2026  
**Database Engine:** H2 Database  
**Database Location:** ~/bdms_v2  
**Version:** 1.0

---

## 📑 Table of Contents
- [Overview](#overview)
- [Core Tables](#core-tables)
- [Table Definitions](#table-definitions)
- [Relationships & Foreign Keys](#relationships--foreign-keys)
- [Indexes](#indexes)
- [Common Queries](#common-queries)
- [Data Integrity Rules](#data-integrity-rules)
- [Backup & Recovery](#backup--recovery)

---

## 🎯 Overview

The BDMS database comprises **8 core tables** organized into functional domains:

| Domain | Tables | Purpose |
|--------|--------|---------|
| **Resident Management** | residents | Resident profiles and demographics |
| **Document Management** | documents, document_requests | Issued documents and requests |
| **Complaint Management** | complaints | Complaint tracking |
| **Announcements** | announcements | Event/alert distribution |
| **Access Control** | users, roles | User authentication and authorization |
| **Audit & Compliance** | audit_log | Operation audit trail |

---

## 📊 Core Tables

### 1. RESIDENTS
**Purpose:** Store resident information and demographics  
**Records:** Estimated 1,000-10,000 per barangay

```sql
CREATE TABLE residents (
    id INT PRIMARY KEY AUTO_INCREMENT,
    firstName VARCHAR(100) NOT NULL,
    lastName VARCHAR(100) NOT NULL,
    middleName VARCHAR(100),
    dateOfBirth DATE,
    gender VARCHAR(20),                 -- Male, Female, Other
    civilStatus VARCHAR(50),             -- Single, Married, Divorced, Widowed
    bloodType VARCHAR(5),                -- O+, A-, B+, etc.
    
    -- Contact Information
    phoneNumber VARCHAR(20),
    emailAddress VARCHAR(255),
    
    -- Address
    street VARCHAR(255),
    barangay VARCHAR(100),
    municipality VARCHAR(100),
    province VARCHAR(100),
    zipCode VARCHAR(10),
    
    -- Utility
    purok VARCHAR(50),                   -- Filipino subdivision
    zone VARCHAR(50),
    
    -- Status
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, DECEASED, TRANSFERRED
    dateAdded DATETIME DEFAULT CURRENT_TIMESTAMP,
    lastUpdated DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedBy VARCHAR(255),
    
    -- Additional Fields
    remarks TEXT,
    photoPath VARCHAR(500),              -- Path to resident photo
    idNumber VARCHAR(50) UNIQUE,         -- Barangay ID number
    
    INDEX idx_lastName (lastName),
    INDEX idx_phoneNumber (phoneNumber),
    INDEX idx_status (status),
    INDEX idx_dateAdded (dateAdded)
);
```

**Sample Data:**
```sql
INSERT INTO residents VALUES (
    1, 'Juan', 'Dela Cruz', 'Santos', '1985-05-15', 'Male', 'Married', 'O+',
    '09123456789', 'juan.delacruz@email.com',
    'Main Street 123', 'San Marino', 'City', 'Province', '1234',
    '01', 'Zone A',
    'ACTIVE', NOW(), NOW(), 'superadmin',
    'Business Owner', '/photos/juan_delacruz.jpg', 'SM-2026-001'
);
```

---

### 2. DOCUMENTS
**Purpose:** Track issued barangay documents and certificates  
**Records:** Estimated 5,000-50,000 annually

```sql
CREATE TABLE documents (
    id INT PRIMARY KEY AUTO_INCREMENT,
    documentNumber VARCHAR(50) UNIQUE NOT NULL,
    residentID INT NOT NULL,
    documentType VARCHAR(100) NOT NULL,  -- Clearance, Certificate, Residency, etc.
    purpose VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, ISSUED, UNCLAIMED, EXPIRED
    
    issuedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    validityDate DATE,                   -- Expiration date
    expiryDate DATE,
    claimedDate DATETIME,
    
    issuedBy VARCHAR(255),               -- Staff member who issued
    claimedBy VARCHAR(255),              -- Person who claimed
    
    fee DECIMAL(10, 2) DEFAULT 0,
    remarks TEXT,
    photoPath VARCHAR(500),              -- Attached photos
    
    INDEX idx_documentNumber (documentNumber),
    INDEX idx_residentID (residentID),
    INDEX idx_documentType (documentType),
    INDEX idx_status (status),
    FOREIGN KEY (residentID) REFERENCES residents(id)
);
```

**Sample Data:**
```sql
INSERT INTO documents VALUES (
    1, 'DOC-2026-001001', 1, 'Barangay Clearance', 'Travel', 'ISSUED',
    NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(),
    'secretary', 'Juan Dela Cruz',
    150.00, 'For travel to Manila', NULL
);
```

---

### 3. COMPLAINTS
**Purpose:** Track submitted complaints and incidents  
**Records:** Estimated 300-1,000 annually

```sql
CREATE TABLE complaints (
    id INT PRIMARY KEY AUTO_INCREMENT,
    complaintNumber VARCHAR(50) UNIQUE NOT NULL,
    residentID INT NOT NULL,
    residentName VARCHAR(255),           -- Denormalized for performance
    
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100),                -- Road/Utility/Safety/Other
    severity VARCHAR(50) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, CRITICAL
    
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, ONGOING, RESOLVED, CLOSED
    dateSubmitted DATETIME DEFAULT CURRENT_TIMESTAMP,
    lastUpdated DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    assignedTo VARCHAR(255),              -- Staff member assigned
    adminNotes TEXT,
    resolutionNotes TEXT,
    
    photoPath VARCHAR(500),               -- Evidence photo
    videoPath VARCHAR(500),               -- Evidence video
    
    dateResolved DATETIME,
    resolvedBy VARCHAR(255),              -- Staff who resolved
    
    INDEX idx_complaintNumber (complaintNumber),
    INDEX idx_residentID (residentID),
    INDEX idx_status (status),
    INDEX idx_dateSubmitted (dateSubmitted),
    FOREIGN KEY (residentID) REFERENCES residents(id)
);
```

**Sample Data:**
```sql
INSERT INTO complaints VALUES (
    1, 'CMPL-2026-001', 1, 'Juan Dela Cruz',
    'Street Pothole on Main St', 'Large pothole near marketplace, safety hazard',
    'Road', 'HIGH',
    'RESOLVED', NOW(), NOW(),
    'secretary', 'Assigned to maintenance', 'Fixed on 04-05-2026',
    '/evidence/pothole.jpg', NULL,
    DATE_SUB(NOW(), INTERVAL 5 DAY), 'secretary'
);
```

---

### 4. ANNOUNCEMENTS
**Purpose:** Publish events, alerts, and programs  
**Records:** Estimated 50-200 annually

```sql
CREATE TABLE announcements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    
    type VARCHAR(50) NOT NULL,            -- Event, Emergency Alert, Program
    category VARCHAR(100),
    
    status VARCHAR(50) DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE, ARCHIVED
    postedBy VARCHAR(255) NOT NULL,
    postedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    lastUpdated DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    startDate DATE,
    endDate DATE,
    
    location VARCHAR(255),
    contact VARCHAR(255),
    imagePath VARCHAR(500),
    
    views INT DEFAULT 0,                  -- View counter
    
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_postedDate (postedDate)
);
```

**Sample Data:**
```sql
INSERT INTO announcements VALUES (
    1, 'Barangay Fiesta 2026', 'Join us for the annual fiesta celebration with games, contests, and cultural programs',
    'Event', 'Festival',
    'ACTIVE', 'secretary', NOW(), NOW(),
    '2026-05-01', '2026-05-03',
    'Barangay Plaza', '09123456789', '/images/fiesta.jpg',
    1250
);
```

---

### 5. DOCUMENT_REQUESTS
**Purpose:** Track document requests and payment information  
**Records:** Estimated 5,000-50,000 annually

```sql
CREATE TABLE document_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    requestNumber VARCHAR(50) UNIQUE NOT NULL,
    residentID INT NOT NULL,
    
    documentType VARCHAR(100) NOT NULL,  -- Clearance, Certificate, Residency
    quantity INT DEFAULT 1,
    
    fee DECIMAL(10, 2) NOT NULL,
    paymentStatus VARCHAR(50) DEFAULT 'UNPAID', -- UNPAID, PAID, CANCELLED, REFUNDED
    paymentMethod VARCHAR(50),          -- Cash, Cheque, Online, etc.
    paymentDate DATE,
    
    requestDate DATE DEFAULT CURRENT_DATE,
    dueDate DATE,
    
    purpose VARCHAR(255),
    remarks TEXT,
    
    processedBy VARCHAR(255),
    processingStatus VARCHAR(50),       -- PENDING, PROCESSING, COMPLETED
    processingDate DATETIME,
    
    INDEX idx_requestNumber (requestNumber),
    INDEX idx_residentID (residentID),
    INDEX idx_paymentStatus (paymentStatus),
    INDEX idx_requestDate (requestDate),
    FOREIGN KEY (residentID) REFERENCES residents(id)
);
```

**Sample Data:**
```sql
INSERT INTO document_requests VALUES (
    1, 'REQ-2026-0001', 1,
    'Barangay Clearance', 1,
    150.00, 'PAID', 'Cash', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY),
    CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY),
    'Medical purposes', 'Urgent', 
    'secretary', 'COMPLETED', NOW()
);
```

---

### 6. USERS
**Purpose:** Store system user accounts  
**Records:** Estimated 10-50 system users

```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,  -- BCrypt hash
    
    firstName VARCHAR(100),
    lastName VARCHAR(100),
    emailAddress VARCHAR(255) UNIQUE,
    phoneNumber VARCHAR(20),
    
    roleID INT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, LOCKED, DISABLED
    
    createdDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    lastLoginDate DATETIME,
    lastPasswordChange DATETIME,
    passwordExpiryDate DATE,
    
    failedLoginAttempts INT DEFAULT 0,
    lockedUntil DATETIME,
    
    INDEX idx_username (username),
    INDEX idx_status (status),
    FOREIGN KEY (roleID) REFERENCES roles(id)
);
```

**Sample Data:**
```sql
INSERT INTO users VALUES (
    1, 'superadmin', 'BCRYPT_HASH_HERE',
    'Super', 'Admin', 'admin@bdms.gov.ph', '09123456789',
    1, 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 90 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 30 DAY),
    DATE_ADD(NOW(), INTERVAL 90 DAY),
    0, NULL
);
```

---

### 7. ROLES
**Purpose:** Define system roles and permissions  
**Records:** 5 system roles

```sql
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    roleName VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    
    -- Permissions (stored as comma-separated values or bit flags)
    canViewResidents BOOLEAN DEFAULT FALSE,
    canEditResidents BOOLEAN DEFAULT FALSE,
    canIssueDocuments BOOLEAN DEFAULT FALSE,
    canManageComplaints BOOLEAN DEFAULT FALSE,
    canPostAnnouncements BOOLEAN DEFAULT FALSE,
    canAccessFinancials BOOLEAN DEFAULT FALSE,
    canAccessSecurity BOOLEAN DEFAULT FALSE,
    canManageUsers BOOLEAN DEFAULT FALSE,
    canViewAuditLog BOOLEAN DEFAULT FALSE,
    
    createdDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_roleName (roleName)
);
```

**Sample Data:**
```sql
-- Super Admin (Full access)
INSERT INTO roles VALUES (
    1, 'Super Admin', 'Full system access',
    TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
    NOW()
);

-- Secretary (Administrative access)
INSERT INTO roles VALUES (
    2, 'Secretary', 'Resident and complaint management',
    TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE,
    NOW()
);

-- Treasurer (Financial access)
INSERT INTO roles VALUES (
    3, 'Treasurer', 'Financial reports access',
    TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE,
    NOW()
);

-- Barangay Captain (Management access)
INSERT INTO roles VALUES (
    4, 'Barangay Captain', 'Oversight and announcements',
    TRUE, FALSE, FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE,
    NOW()
);

-- Resident (Limited access)
INSERT INTO roles VALUES (
    5, 'Resident', 'Self-service complaint submission',
    FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    NOW()
);
```

---

### 8. AUDIT_LOG
**Purpose:** Record all database operations for audit trail  
**Records:** 100,000+ annually (grows continuously)

```sql
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    username VARCHAR(255),
    action VARCHAR(50) NOT NULL,        -- CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT
    tableName VARCHAR(100),
    recordID INT,
    
    -- Change details
    fieldName VARCHAR(255),
    oldValue TEXT,
    newValue TEXT,
    
    -- Context
    ipAddress VARCHAR(50),
    userAgent VARCHAR(500),
    details TEXT,
    
    INDEX idx_timestamp (timestamp),
    INDEX idx_username (username),
    INDEX idx_action (action),
    INDEX idx_tableName (tableName),
    INDEX idx_recordID (recordID)
);
```

**Sample Data:**
```sql
INSERT INTO audit_log VALUES (
    1, NOW(),
    'secretary', 'UPDATE', 'complaints', 1,
    'status', 'PENDING', 'RESOLVED',
    '192.168.1.100', 'Mozilla/5.0...',
    'Updated complaint status via UI'
);
```

---

## 🔗 Relationships & Foreign Keys

### Entity Relationship Diagram
```
┌──────────────────┐
│   RESIDENTS      │
│  (1000+ records) │
└────────┬─────────┘
         │ 1:N
         │
    ┌────┴─────────────────────────────────────┐
    │                                            │
┌───▼─────────┐   ┌────────────────┐   ┌──────▼────────┐
│ DOCUMENTS   │   │   COMPLAINTS   │   │DOCUMENT_REQ   │
│ (issues)    │   │   (reports)    │   │ (payments)    │
└────────────┘   └────────────────┘   └───────────────┘
    │                    │                      │
    │ All via residentID │                      │
    └────────────────────┴──────────────────────┘


┌────────────────────┐
│    USERS (50)      │
└────────┬───────────┘
         │ N:1 roleID
         │
┌────────▼──────────┐
│   ROLES (5)        │
│ - Super Admin     │
│ - Secretary       │
│ - Treasurer       │
│ - Barangay Capt.  │
│ - Resident        │
└───────────────────┘

┌────────────────────┐
│  ANNOUNCEMENTS     │
│ (posted by users)  │
└────────────────────┘

┌────────────────────┐
│   AUDIT_LOG        │
│ (records all ops)  │
└────────────────────┘
```

### Foreign Key Constraints
```sql
-- Residents ↔ Documents
ALTER TABLE documents 
ADD CONSTRAINT fk_documents_residents 
FOREIGN KEY (residentID) REFERENCES residents(id);

-- Residents ↔ Complaints
ALTER TABLE complaints 
ADD CONSTRAINT fk_complaints_residents 
FOREIGN KEY (residentID) REFERENCES residents(id);

-- Residents ↔ Document Requests
ALTER TABLE document_requests 
ADD CONSTRAINT fk_requests_residents 
FOREIGN KEY (residentID) REFERENCES residents(id);

-- Users ↔ Roles
ALTER TABLE users 
ADD CONSTRAINT fk_users_roles 
FOREIGN KEY (roleID) REFERENCES roles(id);
```

---

## 📑 Indexes

### Performance Indexes
```sql
-- Frequently searched columns
CREATE INDEX idx_residents_lastName ON residents(lastName);
CREATE INDEX idx_residents_status ON residents(status);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_announcements_type ON announcements(type);
CREATE INDEX idx_users_username ON users(username);

-- Date range queries
CREATE INDEX idx_complaints_dateSubmitted ON complaints(dateSubmitted);
CREATE INDEX idx_documents_issuedDate ON documents(issuedDate);
CREATE INDEX idx_announcements_postedDate ON announcements(postedDate);
CREATE INDEX idx_requests_requestDate ON document_requests(requestDate);

-- Foreign key lookups
CREATE INDEX idx_documents_residentID ON documents(residentID);
CREATE INDEX idx_complaints_residentID ON complaints(residentID);
CREATE INDEX idx_users_roleID ON users(roleID);

-- Audit log indexes
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_username ON audit_log(username);
```

---

## 📋 Common Queries

### Resident Management
```sql
-- Get resident by ID with related data
SELECT r.*, COUNT(d.id) as doc_count, COUNT(c.id) as complaint_count
FROM residents r
LEFT JOIN documents d ON r.id = d.residentID
LEFT JOIN complaints c ON r.id = c.residentID
WHERE r.id = ?
GROUP BY r.id;

-- Active residents
SELECT * FROM residents WHERE status = 'ACTIVE' ORDER BY lastName;

-- Search residents by name
SELECT * FROM residents 
WHERE firstName LIKE ? OR lastName LIKE ? 
ORDER BY lastName;

-- Residents by barangay zone
SELECT * FROM residents WHERE zone = ? ORDER BY LastName;
```

### Document Management
```sql
-- Issued documents by date range
SELECT d.*, r.firstName, r.lastName
FROM documents d
JOIN residents r ON d.residentID = r.id
WHERE d.issuedDate BETWEEN ? AND ?
AND d.status = 'ISSUED'
ORDER BY d.issuedDate DESC;

-- Paid documents (for financial reports)
SELECT r.*, d.* FROM documents d
JOIN residents r ON d.residentID = r.id
WHERE d.status = 'ISSUED' AND d.issuedDate >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY);

-- Expiring documents
SELECT * FROM documents 
WHERE DATE(expiryDate) <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)
AND status != 'EXPIRED';
```

### Complaint Management
```sql
-- Complaints by resident
SELECT * FROM complaints 
WHERE residentID = ? 
ORDER BY dateSubmitted DESC;

-- Open complaints (not resolved)
SELECT c.*, r.phoneNumber, r.emailAddress
FROM complaints c
JOIN residents r ON c.residentID = r.id
WHERE c.status IN ('PENDING', 'ONGOING')
ORDER BY c.dateSubmitted ASC;

-- Complaints by status summary
SELECT status, COUNT(*) as count
FROM complaints
GROUP BY status;

-- Complaints by severity
SELECT severity, COUNT(*) as count, 
AVG(DATEDIFF(dateResolved, dateSubmitted)) as avg_days
FROM complaints
WHERE dateResolved IS NOT NULL
GROUP BY severity;
```

### Announcement Management
```sql
-- Active announcements
SELECT * FROM announcements 
WHERE status = 'ACTIVE' 
AND endDate >= CURRENT_DATE
ORDER BY postedDate DESC;

-- Announcements by type
SELECT type, COUNT(*) as count FROM announcements 
WHERE status = 'ACTIVE'
GROUP BY type;

-- Recent announcements (dashboard)
SELECT * FROM announcements 
WHERE status = 'ACTIVE'
ORDER BY postedDate DESC
LIMIT 5;
```

### Financial Reports
```sql
-- Daily collections (last 7 days)
SELECT DATE(request_date) as collection_date, SUM(fee) as daily_total
FROM document_requests
WHERE payment_status = 'PAID'
AND request_date >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)
GROUP BY DATE(request_date)
ORDER BY collection_date ASC;

-- Monthly income (last 12 months)
SELECT YEAR(request_date) as year, MONTH(request_date) as month, 
DATE_FORMAT(request_date, '%M %Y') as month_year, SUM(fee) as monthly_total
FROM document_requests
WHERE payment_status = 'PAID'
AND request_date >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH)
GROUP BY YEAR(request_date), MONTH(request_date)
ORDER BY year DESC, month DESC;

-- Top document types by revenue
SELECT documentType, COUNT(*) as count, SUM(fee) as total_revenue
FROM document_requests
WHERE payment_status = 'PAID'
GROUP BY documentType
ORDER BY total_revenue DESC;

-- Outstanding payments
SELECT dr.*, r.firstName, r.lastName, r.phoneNumber
FROM document_requests dr
JOIN residents r ON dr.residentID = r.id
WHERE dr.paymentStatus = 'UNPAID'
AND dr.dueDate <= CURRENT_DATE
ORDER BY dr.dueDate ASC;
```

### User Management & Security
```sql
-- Active users
SELECT u.*, r.roleName
FROM users u
JOIN roles r ON u.roleID = r.id
WHERE u.status = 'ACTIVE'
ORDER BY u.firstName;

-- Locked accounts
SELECT * FROM users 
WHERE status = 'LOCKED' 
AND lockedUntil > NOW();

-- Recent logins (past 7 days)
SELECT username, lastLoginDate
FROM users
WHERE lastLoginDate >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY lastLoginDate DESC;
```

### Audit Trail
```sql
-- User activity
SELECT * FROM audit_log
WHERE username = ?
AND timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY timestamp DESC;

-- Changes to specific record
SELECT * FROM audit_log
WHERE tableName = ? AND recordID = ?
ORDER BY timestamp DESC;

-- Failed login attempts
SELECT * FROM audit_log
WHERE action = 'LOGIN' AND details LIKE '%Failed%'
ORDER BY timestamp DESC
LIMIT 100;

-- Data modifications today
SELECT action, tableName, COUNT(*) as count
FROM audit_log
WHERE DATE(timestamp) = CURRENT_DATE
GROUP BY action, tableName
ORDER BY count DESC;
```

---

## ✅ Data Integrity Rules

### Constraints & Validations
```sql
-- Primary Keys (Uniqueness)
ALTER TABLE residents ADD CONSTRAINT pk_residents PRIMARY KEY (id);
ALTER TABLE documents ADD CONSTRAINT pk_documents PRIMARY KEY (id);

-- Unique Constraints
ALTER TABLE residents ADD CONSTRAINT uq_residents_idNumber UNIQUE (idNumber);
ALTER TABLE documents ADD CONSTRAINT uq_documents_docNumber UNIQUE (documentNumber);
ALTER TABLE users ADD CONSTRAINT uq_users_username UNIQUE (username);

-- Not Null Constraints
ALTER TABLE residents MODIFY firstName VARCHAR(100) NOT NULL;
ALTER TABLE documents MODIFY documentType VARCHAR(100) NOT NULL;
ALTER TABLE complaints MODIFY title VARCHAR(255) NOT NULL;

-- Check Constraints
ALTER TABLE residents ADD CONSTRAINT ck_residents_gender 
CHECK (gender IN ('Male', 'Female', 'Other'));

ALTER TABLE complaints ADD CONSTRAINT ck_complaints_status
CHECK (status IN ('PENDING', 'ONGOING', 'RESOLVED', 'CLOSED'));

-- Default Values
ALTER TABLE residents MODIFY status VARCHAR(50) DEFAULT 'ACTIVE';
ALTER TABLE document_requests MODIFY paymentStatus VARCHAR(50) DEFAULT 'UNPAID';
```

### Referential Integrity
- Deleting a resident automatically cascades to documents, complaints, requests
- Deleting a role prevents deletion of assigned users
- Deleting announcements removes related audit entries

---

## 💾 Backup & Recovery

### Backup Procedures
```sql
-- Full database backup
BACKUP TO '/backup/bdms_2026-04-09_full.zip';

-- Export residents table to CSV
SELECT * FROM residents INTO OUTFILE '/export/residents.csv';

-- Export financial data
SELECT * FROM document_requests INTO OUTFILE '/export/financial.csv';
```

### Recovery Procedures
```sql
-- Restore from backup
RESTORE FROM '/backup/bdms_2026-04-09_full.zip';

-- Point-in-time recovery
-- (Requires transaction logs - configure H2 for WAL mode)

-- Manual recovery from export
LOAD DATA FROM '/export/residents.csv';
```

### Maintenance
```sql
-- Analyze and optimize
ANALYZE;

-- Defragment database
ALTER INDEX idx_complaints_status REBUILD;

-- Check database integrity
CHECK DATABASE;

-- Purge old audit logs (> 1 year)
DELETE FROM audit_log 
WHERE timestamp < DATE_SUB(NOW(), INTERVAL 365 DAY);
```

---

## 📈 Database Statistics

### Growth Estimates
| Table | Annual Growth | 5-Year Projection |
|-------|--------------|-------------------|
| residents | +500 | 3,500+ |
| documents | +10,000 | 50,000+ |
| complaints | +500 | 2,500+ |
| announcements | +200 | 1,000+ |
| document_requests | +10,000 | 50,000+ |
| audit_log | +100,000 | 500,000+ |

### Performance Recommendations
- Partition audit_log by month for faster queries
- Archive documents > 5 years old
- Refresh indexes monthly with ANALYZE
- Monitor query execution plans for slow queries

---

## 🔐 Security

### Password Storage
```java
// BCrypt hashing (12 rounds)
String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
```

### Sensitive Data
- Passwords stored as BCrypt hash only
- Phone numbers not exposed in reports
- Email addresses encrypted in transit
- Audit log accessible to admins only

### Access Control
- Users can only view their own records (except admins)
- Complaints visible only to submitter and assigned staff
- Financial reports restricted to Treasurer + Admin roles

---

## 📞 Database Support

### Connection String
```
jdbc:h2:~/bdms_v2;MODE=MySQL;
```

### Connection Pool Settings
```
maxPoolSize: 20
minPoolSize: 5
connectionTimeout: 10000
idleMaxAge: 600000
```

### Troubleshooting
- Database locked: Restart application
- Connection refused: Check H2 port and permissions
- Out of memory: Increase JVM heap size (`-Xmx1024m`)

---

*Last Updated: April 9, 2026*  
*Database Version: 1.0*  
*Status: Production Ready*
