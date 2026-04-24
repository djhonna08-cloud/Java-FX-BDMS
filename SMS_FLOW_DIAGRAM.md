# SMS Notification System - Flow Diagrams

## 📊 System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         BDMS Application                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐  │
│  │   Document   │      │  Complaint   │      │ Announcement │  │
│  │   Requests   │      │  Management  │      │    Portal    │  │
│  └──────┬───────┘      └──────┬───────┘      └──────┬───────┘  │
│         │                     │                      │           │
│         └─────────────────────┼──────────────────────┘           │
│                               │                                  │
│                               ▼                                  │
│                    ┌──────────────────┐                          │
│                    │   SMSService     │                          │
│                    │  (SMS Gateway)   │                          │
│                    └────────┬─────────┘                          │
│                             │                                    │
│         ┌───────────────────┼───────────────────┐                │
│         │                   │                   │                │
│         ▼                   ▼                   ▼                │
│  ┌─────────────┐   ┌──────────────┐   ┌──────────────┐         │
│  │ sms_config  │   │   sms_log    │   │sms_templates │         │
│  │  (Config)   │   │  (Logging)   │   │ (Templates)  │         │
│  └─────────────┘   └──────────────┘   └──────────────┘         │
│                                                                   │
└───────────────────────────────┬───────────────────────────────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │   Semaphore API       │
                    │  (SMS Gateway)        │
                    │  api.semaphore.co     │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │  Philippine Mobile    │
                    │  Networks (Globe,     │
                    │  Smart, Sun, etc.)    │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │   Resident's Phone    │
                    │   (SMS Received)      │
                    └───────────────────────┘
```

---

## 🔄 SMS Sending Flow

### Standard SMS Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. User Action (e.g., Approve Document Request)                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. Get Resident Phone Number from Database                      │
│    SELECT phone_number FROM residents WHERE id = ?              │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. Get SMS Template from Database                               │
│    SELECT template FROM sms_templates WHERE name = ?            │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. Replace Template Placeholders                                │
│    message = template.replace("{document_type}", "Clearance")   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. Call SMSService.sendSMS(phoneNumber, message)                │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. SMSService Validates Phone Number                            │
│    - Normalize: 09171234567 → 639171234567                      │
│    - Validate: Must start with 639                              │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 7. Check SMS Configuration                                      │
│    - Is SMS enabled?                                            │
│    - Is API key configured?                                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 8. Check Rate Limit (120 calls/minute)                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 9. Send HTTP POST to Semaphore API                              │
│    POST https://api.semaphore.co/api/v4/messages                │
│    Body: apikey, number, message, sendername                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 10. Parse Semaphore Response                                    │
│     Success: {"message_id": 123456, "status": "Pending"}        │
│     Error: {"error": "Invalid API key"}                         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 11. Log SMS Transaction to Database                             │
│     INSERT INTO sms_log (phone_number, message, status, ...)    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 12. Return SMSResponse to Caller                                │
│     response.isSuccess() → true/false                           │
│     response.getMessage() → "SMS sent successfully"             │
│     response.getMessageId() → "123456"                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📱 Document Request SMS Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                    Document Request Workflow                      │
└──────────────────────────────────────────────────────────────────┘

Resident Submits Request
         │
         ▼
┌─────────────────────┐
│ Request Created     │
│ Status: PENDING     │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────┐
    │ No SMS Sent  │  (Optional: Send "Request Received" SMS)
    └──────────────┘
           │
           ▼
┌─────────────────────┐
│ Admin Reviews       │
│ Request             │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Admin Approves      │
│ Status: APPROVED    │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────────────────────────────┐
    │ 📱 SMS: "Document Approved"          │
    │ "Your Barangay Clearance request     │
    │  has been approved. You may claim    │
    │  it at the Barangay Hall."           │
    └──────────────┬───────────────────────┘
                   │
                   ▼
┌─────────────────────┐
│ Document Printed    │
│ Status: READY       │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────────────────────────────┐
    │ 📱 SMS: "Document Ready"             │
    │ "Your Barangay Clearance is now      │
    │  ready for pickup. Please bring      │
    │  a valid ID."                        │
    └──────────────┬───────────────────────┘
                   │
                   ▼
┌─────────────────────┐
│ Resident Claims     │
│ Status: CLAIMED     │
└─────────────────────┘
```

---

## 🚨 Complaint SMS Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                    Complaint Management Workflow                  │
└──────────────────────────────────────────────────────────────────┘

Resident Submits Complaint
         │
         ▼
┌─────────────────────┐
│ Complaint Created   │
│ Status: PENDING     │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────────────────────────────┐
    │ 📱 SMS: "Complaint Received"         │
    │ "Your complaint 'Street Pothole      │
    │  on Main St' has been received.      │
    │  Reference: CMPL-2026-001"           │
    └──────────────┬───────────────────────┘
                   │
                   ▼
┌─────────────────────┐
│ Admin Assigns       │
│ Status: ONGOING     │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────────────────────────────┐
    │ 📱 SMS: "Complaint Update"           │
    │ "Your complaint is being             │
    │  addressed. Assigned to:             │
    │  Maintenance Team"                   │
    └──────────────┬───────────────────────┘
                   │
                   ▼
┌─────────────────────┐
│ Issue Resolved      │
│ Status: RESOLVED    │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────────────────────────────┐
    │ 📱 SMS: "Complaint Resolved"         │
    │ "Your complaint 'Street Pothole      │
    │  on Main St' has been resolved.      │
    │  Thank you for your patience."       │
    └──────────────────────────────────────┘
```

---

## 📢 Announcement SMS Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                    Announcement Broadcast Workflow                │
└──────────────────────────────────────────────────────────────────┘

Admin Creates Announcement
         │
         ▼
┌─────────────────────┐
│ Announcement Posted │
│ Type: Event         │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────────┐
    │ Admin Selects:   │
    │ ☑ Send SMS       │
    │ ☐ Email Only     │
    └──────┬───────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ Get All Resident Phone Numbers      │
│ SELECT phone_number FROM residents  │
│ WHERE phone_number IS NOT NULL      │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ Prepare Bulk SMS                    │
│ phoneNumbers[] = [639171234567,     │
│                   639181234567, ...] │
│ message = "Barangay Fiesta 2026:    │
│            Join us for games..."    │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ Send Bulk SMS                       │
│ SMSService.sendBulkSMS(             │
│   phoneNumbers, message)            │
└──────────┬──────────────────────────┘
           │
           ▼
    ┌──────────────────────────────────────┐
    │ 📱 SMS to All Residents:             │
    │ "Barangay Fiesta 2026: Join us      │
    │  for games, contests, and cultural   │
    │  programs. May 1-3, 2026 at          │
    │  Barangay Plaza."                    │
    └──────────────────────────────────────┘
```

---

## 🔐 OTP Verification Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                    OTP Verification Workflow                      │
└──────────────────────────────────────────────────────────────────┘

User Requests Password Reset
         │
         ▼
┌─────────────────────┐
│ Generate 6-digit    │
│ OTP Code            │
│ otp = "123456"      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Store OTP in        │
│ Database with       │
│ Expiry (10 min)     │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────────────────────────────┐
    │ 📱 SMS: "OTP Code"                   │
    │ "Your verification code is: 123456.  │
    │  Valid for 10 minutes. Do not        │
    │  share this code."                   │
    └──────────────┬───────────────────────┘
                   │
                   ▼
┌─────────────────────┐
│ User Enters OTP     │
│ on Website/App      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Verify OTP          │
│ - Correct?          │
│ - Not Expired?      │
└──────────┬──────────┘
           │
           ├─── Valid ───────────────┐
           │                         ▼
           │              ┌─────────────────────┐
           │              │ Allow Password      │
           │              │ Reset               │
           │              └─────────────────────┘
           │
           └─── Invalid ─────────────┐
                                     ▼
                          ┌─────────────────────┐
                          │ Show Error:         │
                          │ "Invalid or         │
                          │  Expired OTP"       │
                          └─────────────────────┘
```

---

## 📊 SMS Statistics Dashboard

```
┌──────────────────────────────────────────────────────────────────┐
│                    SMS Statistics Dashboard                       │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Total SMS Sent Today: 45                                        │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                   │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   ✅ Sent       │  │   ❌ Failed     │  │   ⏳ Pending    │ │
│  │      38         │  │       5         │  │       2         │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                   │
│  SMS by Type:                                                    │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                   │
│  📄 Document Notifications:  20  ████████████████░░░░░░░░░░░░   │
│  🚨 Complaint Updates:       10  ████████░░░░░░░░░░░░░░░░░░░░   │
│  📢 Announcements:            8  ██████░░░░░░░░░░░░░░░░░░░░░░   │
│  🔐 OTP Codes:                5  ████░░░░░░░░░░░░░░░░░░░░░░░░   │
│  💰 Payment Reminders:        2  ██░░░░░░░░░░░░░░░░░░░░░░░░░░   │
│                                                                   │
│  Recent SMS Log:                                                 │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                   │
│  ┌────────────┬──────────────┬─────────┬────────────────────┐   │
│  │ Time       │ Phone        │ Status  │ Message            │   │
│  ├────────────┼──────────────┼─────────┼────────────────────┤   │
│  │ 10:30 AM   │ 09171234567  │ ✅ SENT │ Document Approved  │   │
│  │ 10:25 AM   │ 09181234567  │ ✅ SENT │ Complaint Resolved │   │
│  │ 10:20 AM   │ 09191234567  │ ❌ FAIL │ Invalid Number     │   │
│  │ 10:15 AM   │ 09201234567  │ ✅ SENT │ OTP: 123456        │   │
│  │ 10:10 AM   │ 09211234567  │ ✅ SENT │ Payment Reminder   │   │
│  └────────────┴──────────────┴─────────┴────────────────────┘   │
│                                                                   │
│  [View Full Log]  [Export Report]  [SMS Settings]               │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Error Handling Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                    SMS Error Handling Flow                        │
└──────────────────────────────────────────────────────────────────┘

SMSService.sendSMS(phoneNumber, message)
         │
         ▼
┌─────────────────────┐
│ Validate Phone      │
│ Number              │
└──────────┬──────────┘
           │
           ├─── Invalid ──────────────┐
           │                          ▼
           │               ┌─────────────────────┐
           │               │ Return Error:       │
           │               │ "Invalid Philippine │
           │               │  mobile number"     │
           │               └─────────────────────┘
           │
           ├─── Valid ────────────────┐
           │                          ▼
           │               ┌─────────────────────┐
           │               │ Check SMS Config    │
           │               └──────────┬──────────┘
           │                          │
           │                          ├─── Not Configured ───┐
           │                          │                      ▼
           │                          │           ┌─────────────────────┐
           │                          │           │ Return Error:       │
           │                          │           │ "SMS API key not    │
           │                          │           │  configured"        │
           │                          │           └─────────────────────┘
           │                          │
           │                          ├─── Disabled ─────────┐
           │                          │                      ▼
           │                          │           ┌─────────────────────┐
           │                          │           │ Return Error:       │
           │                          │           │ "SMS notifications  │
           │                          │           │  are disabled"      │
           │                          │           └─────────────────────┘
           │                          │
           │                          ├─── Configured & Enabled ─┐
           │                          │                           ▼
           │                          │                ┌─────────────────────┐
           │                          │                │ Check Rate Limit    │
           │                          │                └──────────┬──────────┘
           │                          │                           │
           │                          │                           ├─── Exceeded ───┐
           │                          │                           │                ▼
           │                          │                           │     ┌─────────────────────┐
           │                          │                           │     │ Return Error:       │
           │                          │                           │     │ "Rate limit         │
           │                          │                           │     │  exceeded"          │
           │                          │                           │     └─────────────────────┘
           │                          │                           │
           │                          │                           ├─── OK ─────────┐
           │                          │                           │                ▼
           │                          │                           │     ┌─────────────────────┐
           │                          │                           │     │ Send to Semaphore   │
           │                          │                           │     │ API                 │
           │                          │                           │     └──────────┬──────────┘
           │                          │                           │                │
           │                          │                           │                ├─── API Error ───┐
           │                          │                           │                │                 ▼
           │                          │                           │                │      ┌─────────────────────┐
           │                          │                           │                │      │ Log Error           │
           │                          │                           │                │      │ Return Error        │
           │                          │                           │                │      └─────────────────────┘
           │                          │                           │                │
           │                          │                           │                ├─── Success ─────┐
           │                          │                           │                │                 ▼
           │                          │                           │                │      ┌─────────────────────┐
           │                          │                           │                │      │ Log Success         │
           │                          │                           │                │      │ Return Success      │
           │                          │                           │                │      └─────────────────────┘
```

---

*These diagrams illustrate the complete SMS notification system flow in BDMS.*
