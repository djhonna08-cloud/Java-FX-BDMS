-- Update User Credentials SQL Script
-- Run this in H2 Console to update all user passwords

-- Update or Insert Super Admin
MERGE INTO users (username, password, role) KEY(username) VALUES ('superadmin', 'admin123', 'Super Admin');

-- Update or Insert Owner
MERGE INTO users (username, password, role) KEY(username) VALUES ('owner', 'owner123', 'Owner');

-- Update or Insert Barangay Captain
MERGE INTO users (username, password, role) KEY(username) VALUES ('captain', 'captain123', 'Barangay Captain');

-- Update or Insert Barangay Secretary
MERGE INTO users (username, password, role) KEY(username) VALUES ('secretary', 'secretary123', 'Barangay Secretary');

-- Update or Insert Barangay Treasurer
MERGE INTO users (username, password, role) KEY(username) VALUES ('treasurer', 'treasurer123', 'Barangay Treasurer');

-- Update or Insert Kagawad
MERGE INTO users (username, password, role) KEY(username) VALUES ('kagawad', 'kagawad123', 'Kagawads');

-- Update or Insert Health Worker
MERGE INTO users (username, password, role) KEY(username) VALUES ('healthworker', 'health123', 'Barangay Health Workers');

-- Update or Insert Tanod
MERGE INTO users (username, password, role) KEY(username) VALUES ('tanod', 'tanod123', 'Barangay Tanods');

-- Verify the updates
SELECT username, password, role FROM users ORDER BY username;
