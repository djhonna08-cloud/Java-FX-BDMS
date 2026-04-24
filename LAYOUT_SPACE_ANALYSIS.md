# Layout Space Analysis - Barangay San Marino BDMS

## Application Window Dimensions
- **Total Window Size**: 1280px × 900px
- **Window Type**: Fixed size (non-resizable)

---

## Horizontal Space Breakdown

### Sidebar (Left Panel)
- **Expanded Width**: 240px
- **Collapsed Width**: 60px
- **Default State**: Expanded (240px)

### Main Content Area (Right Panel)
When sidebar is **expanded**:
- **Available Width**: 1280px - 240px = **1040px**

When sidebar is **collapsed**:
- **Available Width**: 1280px - 60px = **1220px**

---

## Vertical Space Breakdown

### Top Bar
- **Height**: ~60-70px (estimated from padding and content)
- **Components**:
  - Search field: 320px width
  - Scan button
  - User profile section
- **Padding**: 12px top, 18px horizontal, 0px bottom

### Main Content Area (Scrollable)
- **Available Height**: 900px - 60px (top bar) = **840px**
- **Content Padding**: 20px all sides
- **Effective Content Height**: ~800px

---

## Component-Specific Dimensions

### Login Screen
- **Login Card Width**: 360px (max-width)
- **Logo Width**: 280px
- **Background Image**: 1280px × 900px (full screen)

### Dashboard Content Cards

#### Stat Cards (Overview)
- **Preferred Width**: 95px
- **Min Width**: 90px
- **Max Width**: 100px
- **Min Height**: 120px
- **Padding**: 24px

#### Chart Section
- **Preferred Width**: 500px
- **Max Width**: 600px

#### Announcements Section
- **Preferred Width**: 400px
- **Max Width**: 500px

#### News Card Titles
- **Max Width**: 300px

### Tables

#### Roles Management Table
- **Preferred Height**: 400px
- **Column Widths**:
  - ID: 60px
  - Role Name: 180px
  - Description: 350px

#### Permissions Matrix Table
- **Column Widths**:
  - Role: 180px (min 180px)
  - Each Permission Column: 100-120px

#### Document Requests Table
- **Column Widths**:
  - Resident: 180px
  - Document Type: 150px
  - Request Date: 120px

### Form Elements

#### Standard Input Fields
- **Height**: 44px (pref-height from CSS)
- **Padding**: 12px vertical, 16px horizontal
- **Border Radius**: 8px

#### Search Fields
- **Width**: 320px (top bar search)
- **Width**: 300px (dialog search)
- **Height**: 40-44px

#### Buttons
- **Primary/Standard**: 44px height, 120px min-width
- **Small**: 32px height, 70px min-width
- **Large**: 44px height, 140px min-width
- **Padding**: 14px vertical, 28px horizontal (standard)

#### Combo Boxes
- **Height**: 40-44px
- **Width**: 300px (typical)

#### Text Areas
- **Preferred Height**: 120px

### Dialog Components

#### Resident List View (Dialogs)
- **Preferred Height**: 150px
- **Search Field Width**: 300px

---

## Space Utilization Summary

### When Sidebar is Expanded (240px)
```
┌─────────────────────────────────────────────────────────┐
│                    1280px Total Width                    │
├──────────┬──────────────────────────────────────────────┤
│          │                                               │
│ Sidebar  │         Main Content Area                     │
│  240px   │            1040px                             │
│          │                                               │
│          │  ┌─────────────────────────────────────┐     │
│          │  │ Top Bar (~60-70px height)           │     │
│          │  ├─────────────────────────────────────┤     │
│          │  │                                      │     │
│          │  │ Scrollable Content                   │     │
│          │  │ (~840px height)                      │     │
│          │  │                                      │     │
│          │  │ Effective: ~1000px × ~800px          │     │
│          │  │ (with 20px padding)                  │     │
│          │  │                                      │     │
│          │  └─────────────────────────────────────┘     │
└──────────┴──────────────────────────────────────────────┘
```

### When Sidebar is Collapsed (60px)
```
┌─────────────────────────────────────────────────────────┐
│                    1280px Total Width                    │
├───┬─────────────────────────────────────────────────────┤
│   │                                                      │
│ S │         Main Content Area                            │
│ 60│            1220px                                    │
│   │                                                      │
│   │  ┌──────────────────────────────────────────┐      │
│   │  │ Top Bar (~60-70px height)                │      │
│   │  ├──────────────────────────────────────────┤      │
│   │  │                                           │      │
│   │  │ Scrollable Content                        │      │
│   │  │ (~840px height)                           │      │
│   │  │                                           │      │
│   │  │ Effective: ~1180px × ~800px               │      │
│   │  │ (with 20px padding)                       │      │
│   │  │                                           │      │
│   │  └──────────────────────────────────────────┘      │
└───┴─────────────────────────────────────────────────────┘
```

---

## Key Measurements for Design

### Horizontal Space Available for Content
- **Sidebar Expanded**: ~1000px (1040px - 40px padding)
- **Sidebar Collapsed**: ~1180px (1220px - 40px padding)

### Vertical Space Available for Content
- **Total**: ~800px (840px - 40px padding)
- **Scrollable**: Yes (vertical scroll enabled)

### Recommended Content Widths
- **Full-width tables**: 950-1000px (expanded sidebar)
- **Two-column layout**: 480px each (with 20px gap)
- **Three-column layout**: 310px each (with 20px gaps)
- **Cards in grid**: 90-180px per card

### Recommended Content Heights
- **Tables**: 400-600px
- **Charts**: 300-500px
- **Forms**: Variable (scrollable)
- **Stat cards**: 120px minimum

---

## CSS-Defined Dimensions

### From light-theme.css

#### Sidebar
- Width controlled by Java constants (240px/60px)
- Background: Dark blue gradient (#0A3D62 to #083049)

#### Content Cards
- Border radius: 12px
- Padding: 24-28px
- Min height: 120px (stat cards)

#### Form Inputs
- Height: 44px
- Border radius: 8px
- Padding: 12px × 16px

#### Buttons
- Standard height: 44px
- Small height: 32px
- Border radius: 8px

---

## Notes
- All measurements are in pixels (px)
- The application uses a fixed window size (1280×900)
- Main content area is scrollable vertically
- Sidebar can toggle between 240px and 60px
- Content padding is consistently 20px
- Card padding is consistently 24-28px
