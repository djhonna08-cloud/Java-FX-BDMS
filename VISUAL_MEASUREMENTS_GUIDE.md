# Visual Measurements Guide

## 📐 Exact Measurements for All Components

This guide provides precise measurements for implementing UI components.

---

## 🖥️ Application Layout

```
┌─────────────────────────────────────────────────────────────┐
│                    1280px Total Width                        │
│                     900px Total Height                       │
├──────────┬──────────────────────────────────────────────────┤
│          │  Top Bar (64px height)                           │
│ Sidebar  ├──────────────────────────────────────────────────┤
│  240px   │                                                   │
│  (exp)   │  Main Content Area                               │
│   or     │  Width: 1040px (expanded) / 1220px (collapsed)  │
│  60px    │  Height: 836px                                   │
│  (col)   │  Padding: 20px                                   │
│          │  Effective: 1000px × 796px                       │
│          │                                                   │
└──────────┴──────────────────────────────────────────────────┘
```

---

## 🎛️ Sidebar Measurements

### Expanded State (240px)
```
┌─────────────────────────┐
│  Toggle Button          │  Height: 40px
│  Padding: 8px 0         │  Border-bottom: 1px
├─────────────────────────┤
│  Logo Container         │  Height: ~80px
│  Padding: 16px 0        │  Centered
├─────────────────────────┤
│  Navigation Menu        │
│  Padding: 12px 8px      │
│                         │
│  ┌───────────────────┐ │
│  │ Nav Button        │ │  Height: 44px
│  │ Padding: 12px 16px│ │  Margin: 4px
│  │ Icon: 18px        │ │  Gap: 12px
│  │ Font: 14px        │ │  Radius: 8px
│  └───────────────────┘ │
│                         │
│  [More buttons...]      │
│                         │
│  ┌───────────────────┐ │
│  │ Logout (bottom)   │ │
│  └───────────────────┘ │
└─────────────────────────┘
```

### Collapsed State (60px)
```
┌──────┐
│  ⬅  │  Toggle: 40px
├──────┤
│      │  Logo: Hidden
├──────┤
│  🏠  │  Icon only: 48px
│      │  Centered
├──────┤
│  👥  │  Icon: 18px
├──────┤
│  📋  │
├──────┤
│  ⚙️  │
└──────┘
```

---

## 📊 Top Bar Measurements

```
┌────────────────────────────────────────────────────────────┐
│  Padding: 16px 20px                    Height: 64px        │
│                                                             │
│  ┌──────────────┐  ┌────┐  [Spacer]  ┌──────────────┐   │
│  │ Search Field │  │ 📷 │             │ User Profile │   │
│  │ 320px × 40px │  │40px│             │              │   │
│  │ Padding:     │  └────┘             │ Name: 15px   │   │
│  │ 10px 12px    │                     │ Role: 13px   │   │
│  │ 10px 40px    │  Gap: 16px          │              │   │
│  └──────────────┘                     └──────────────┘   │
└────────────────────────────────────────────────────────────┘
```

---

## 🔘 Button Measurements

### Small Button (32px)
```
┌──────────────────┐
│   Button Text    │  Height: 32px
│   Font: 13px     │  Padding: 8px 16px
│   Min-width: 80px│  Radius: 8px
└──────────────────┘
```

### Standard Button (40px)
```
┌────────────────────┐
│   Button Text      │  Height: 40px
│   Font: 15px       │  Padding: 12px 24px
│   Min-width: 100px │  Radius: 8px
└────────────────────┘
```

### Large Button (48px)
```
┌──────────────────────┐
│   Button Text        │  Height: 48px
│   Font: 16px         │  Padding: 14px 28px
│   Min-width: 120px   │  Radius: 8px
└──────────────────────┘
```

---

## 📝 Form Input Measurements

### Small Input (36px)
```
┌─────────────────────────┐
│ Placeholder text        │  Height: 36px
│ Font: 13px              │  Padding: 8px 12px
└─────────────────────────┘  Border: 1px
                             Radius: 8px
```

### Standard Input (44px)
```
┌─────────────────────────┐
│ Placeholder text        │  Height: 44px
│ Font: 15px              │  Padding: 12px 16px
└─────────────────────────┘  Border: 1px → 2px (focus)
                             Radius: 8px
```

### Large Input (48px)
```
┌─────────────────────────┐
│ Placeholder text        │  Height: 48px
│ Font: 16px              │  Padding: 14px 16px
└─────────────────────────┘  Border: 1px
                             Radius: 8px
```

---

## 🃏 Card Measurements

### Small Card
```
┌─────────────────────────┐
│  Padding: 16px          │
│                         │
│  Content here           │
│                         │
│  Border: 1px            │
│  Radius: 8px            │
│  Shadow: Level 1        │
└─────────────────────────┘
```

### Standard Card
```
┌─────────────────────────┐
│  Padding: 20px          │
│                         │
│  Content here           │
│                         │
│  Border: 1px            │
│  Radius: 12px           │
│  Shadow: Level 2        │
└─────────────────────────┘
```

### Large Card
```
┌─────────────────────────┐
│  Padding: 24px          │
│                         │
│  Content here           │
│                         │
│  Border: 1px            │
│  Radius: 12px           │
│  Shadow: Level 3        │
└─────────────────────────┘
```

---

## 📊 Stat Card Measurements

```
┌─────────────────┐
│   Padding: 20px │  Min-width: 100px
│                 │  Pref-width: 120px
│      1,234      │  Max-width: 250px
│   Font: 36px    │  Min-height: 120px
│   Bold (700)    │
│                 │  Border: 1px
│  STAT TITLE     │  Radius: 12px
│  Font: 13px     │  Shadow: Level 2
│  Semibold (600) │
│                 │  Hover: Scale 1.02
└─────────────────┘  Shadow: Level 3
```

---

## 📋 Table Measurements

### Table Container
```
┌─────────────────────────────────────────┐
│  Padding: 16px                          │  Border: 1px
│  ┌───────────────────────────────────┐ │  Radius: 12px
│  │ Header Row                        │ │  Shadow: Level 2
│  │ Padding: 12px 10px                │ │
│  │ Font: 14px Bold                   │ │
│  │ Background: #f8fafc               │ │
│  ├───────────────────────────────────┤ │
│  │ Data Row (40px height)            │ │
│  │ Padding: 10px                     │ │
│  │ Font: 13px Normal                 │ │
│  │ Background: #ffffff               │ │
│  ├───────────────────────────────────┤ │
│  │ Data Row (40px height)            │ │
│  │ Background: #fafbfc (odd)         │ │
│  ├───────────────────────────────────┤ │
│  │ Data Row (40px height)            │ │
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### Enhanced Table with Search
```
┌─────────────────────────────────────────┐
│  Search Controls                        │  Padding: 12px 16px
│  ┌──────────┐ ┌────────┐ ┌──────────┐ │  Height: 60px
│  │ Search   │ │ Filter │ │ Add New  │ │  Border: 1px (top/sides)
│  │ 36px     │ │ 36px   │ │ 36px     │ │  Radius: 12px 12px 0 0
│  └──────────┘ └────────┘ └──────────┘ │
├─────────────────────────────────────────┤
│  Table (as above)                       │  Border: 1px (sides/bottom)
│                                         │  Radius: 0 0 12px 12px
└─────────────────────────────────────────┘
```

---

## 🏷️ Badge Measurements

### Standard Badge
```
┌──────────┐
│  Badge   │  Padding: 6px 12px
└──────────┘  Font: 11px Bold
              Radius: 12px
```

### Small Badge
```
┌────────┐
│ Badge  │  Padding: 4px 8px
└────────┘  Font: 10px Bold
            Radius: 8px
```

### Large Badge
```
┌────────────┐
│   Badge    │  Padding: 8px 16px
└────────────┘  Font: 12px Bold
                Radius: 16px
```

---

## 📏 Spacing Examples

### Form Layout
```
┌─────────────────────────┐
│  Form Section           │  Padding: 24px
│  ↕ 16px                 │  Spacing: 16px
│  ┌───────────────────┐ │
│  │ Label (14px)      │ │
│  │ ↕ 4px             │ │
│  │ Input (44px)      │ │
│  └───────────────────┘ │
│  ↕ 16px                 │
│  ┌───────────────────┐ │
│  │ Label             │ │
│  │ ↕ 4px             │ │
│  │ Input             │ │
│  └───────────────────┘ │
│  ↕ 16px                 │
│  ┌─────────┐           │
│  │ Button  │           │
│  └─────────┘           │
└─────────────────────────┘
```

### Button Group
```
┌──────────┐ ←8px→ ┌──────────┐ ←8px→ ┌──────────┐
│  Save    │       │  Cancel  │       │  Delete  │
└──────────┘       └──────────┘       └──────────┘
```

### Card Grid
```
┌────────┐ ←20px→ ┌────────┐ ←20px→ ┌────────┐
│ Card 1 │        │ Card 2 │        │ Card 3 │
└────────┘        └────────┘        └────────┘
    ↕ 20px            ↕ 20px            ↕ 20px
┌────────┐        ┌────────┐        ┌────────┐
│ Card 4 │        │ Card 5 │        │ Card 6 │
└────────┘        └────────┘        └────────┘
```

---

## 🎨 Shadow Measurements

### Level 1 (Subtle)
```
Blur: 8px
Offset: 0px, 2px
Color: rgba(0, 0, 0, 0.05)
Use: Small cards, subtle elevation
```

### Level 2 (Standard)
```
Blur: 12px
Offset: 0px, 4px
Color: rgba(0, 0, 0, 0.08)
Use: Standard cards, buttons
```

### Level 3 (Elevated)
```
Blur: 16px
Offset: 0px, 6px
Color: rgba(0, 0, 0, 0.12)
Use: Important cards, hover states
```

### Level 4 (Floating)
```
Blur: 20px
Offset: 0px, 8px
Color: rgba(0, 0, 0, 0.15)
Use: Modals, popovers, dropdowns
```

---

## 📐 Typography Line Heights

```
Display (28px):  Line-height: 36px (1.3)
Section (20px):  Line-height: 28px (1.4)
Body (14px):     Line-height: 20px (1.4)
Small (13px):    Line-height: 18px (1.4)
Micro (11px):    Line-height: 16px (1.45)
```

---

## 🎯 Quick Reference Table

| Element | Width | Height | Padding | Font | Radius |
|---------|-------|--------|---------|------|--------|
| **Sidebar (exp)** | 240px | 900px | 12px 8px | 14px | 8px |
| **Sidebar (col)** | 60px | 900px | 12px 0 | - | 8px |
| **Top Bar** | 1040px | 64px | 16px 20px | 15px | - |
| **Button (sm)** | 80px+ | 32px | 8px 16px | 13px | 8px |
| **Button (std)** | 100px+ | 40px | 12px 24px | 15px | 8px |
| **Button (lg)** | 120px+ | 48px | 14px 28px | 16px | 8px |
| **Input (sm)** | - | 36px | 8px 12px | 13px | 8px |
| **Input (std)** | - | 44px | 12px 16px | 15px | 8px |
| **Input (lg)** | - | 48px | 14px 16px | 16px | 8px |
| **Card (sm)** | - | - | 16px | - | 8px |
| **Card (std)** | - | - | 20px | - | 12px |
| **Card (lg)** | - | - | 24px | - | 12px |
| **Table Header** | - | - | 12px 10px | 14px | - |
| **Table Cell** | - | 40px | 10px | 13px | - |
| **Badge (sm)** | - | - | 4px 8px | 10px | 8px |
| **Badge (std)** | - | - | 6px 12px | 11px | 12px |
| **Badge (lg)** | - | - | 8px 16px | 12px | 16px |

---

## 📱 Responsive Breakpoints

While the application is fixed at 1280×900, here are the effective content widths:

| State | Sidebar | Content Width | Effective Width |
|-------|---------|---------------|-----------------|
| **Expanded** | 240px | 1040px | ~1000px (with padding) |
| **Collapsed** | 60px | 1220px | ~1180px (with padding) |

---

**Use this guide for precise implementation!** 📐✨
