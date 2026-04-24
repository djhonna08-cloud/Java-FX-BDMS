# JavaFX Technical Dashboard - Quick Reference Guide

## 🎨 Color Palette

### Primary Colors
```
Dark Blue (Primary)    #0A3D62  ███████  High-trust elements
Gold (Secondary)       #FBC531  ███████  Leadership accents
Surface White          #F8FAFC  ███████  Card backgrounds
Border Slate           #e2e8f0  ███████  Surgical borders
```

### Text Colors
```
Primary Text           #0f172a  ███████  Headings, labels
Secondary Text         #475569  ███████  Body text, descriptions
Tertiary Text          #94a3b8  ███████  Subtle text, hints
```

### Semantic Colors
```
Success Green          #10b981  ███████  Completed, approved
Warning Orange         #f59e0b  ███████  Pending, attention needed
Error Red              #ef4444  ███████  Critical, rejected
Info Blue              #3b82f6  ███████  Information, neutral
```

---

## 🔤 Typography Scale

```
Display Heading        Outfit, 28px, Bold (700)
Section Heading        Outfit, 20px, Bold (700)
Stat Card Value        Outfit, 36px, Bold (700)
Body Text              Inter, 16px, Regular (400)
Form Labels            Inter, 14px, Semi-Bold (600)
Table Headers          Inter, 14px, Bold (700)
Buttons                Inter, 16px, Bold (700)
Badges                 Inter, 12px, Bold (700)
```

---

## 🧩 Component Classes Quick Reference

### Buttons
```css
.button-primary         /* Dark blue gradient, white text */
.button-secondary       /* Gold background, dark text */
.button-tertiary        /* Transparent with dark blue border */
.button-danger          /* Red background, white text */
.button-accent          /* Gold background (Captain's actions) */
```

### Cards
```css
.stat-card              /* Statistics display cards */
.content-box            /* General content containers */
.quick-action-card      /* Dark blue gradient action cards */
.captain-action-card    /* Gold gradient leadership cards */
.news-card              /* Announcement feed cards */
```

### Workflow Tracking
```css
.workflow-step                  /* Base step style */
.workflow-step-active           /* Current step (Dark Blue) */
.workflow-step-completed        /* Done steps (Green) */
.workflow-step-pending          /* Future steps (Gray) */
.workflow-step-pulse            /* Animated active step */
```

### Priority Indicators
```css
.priority-high          /* Red border, urgent */
.priority-medium        /* Orange border, moderate */
.priority-low           /* Blue border, routine */
```

### Badges
```css
.badge-success          /* Green semi-transparent */
.badge-warning          /* Orange semi-transparent */
.badge-error            /* Red semi-transparent */
.badge-info             /* Blue semi-transparent */
.badge-gold             /* Gold semi-transparent */
```

### Impact Levels
```css
.impact-critical        /* Red solid background */
.impact-normal          /* Blue solid background */
```

### Security
```css
.security-indicator             /* Security status container */
.security-indicator-active      /* Active with glow */
.encryption-badge               /* AES-256 badge */
```

### Forms
```css
.text-field             /* Standard input fields */
.input-standard         /* 44px height inputs */
.form-label             /* Bold form labels */
.form-section           /* Form container with spacing */
```

### Tables
```css
.table-view             /* Main table container */
.table-row-cell:hover   /* Hover state */
.table-row-cell:selected /* Selected row (Dark Blue) */
```

### Misc
```css
.upload-zone            /* Dashed border drag-and-drop */
.progress-bar           /* Revenue tracking bars */
.trend-up               /* Green percentage increase */
.trend-down             /* Red percentage decrease */
```

---

## 📐 Spacing System

```
Extra Small    4px     Tight spacing
Small          8px     Icon gaps
Medium         12px    Button padding
Standard       16px    Section spacing
Large          20px    Module spacing
Extra Large    24px    Major sections
Huge           28px    Content box padding
```

---

## 🎯 Border Radius Scale

```
Small          6px     Badges, small elements
Standard       8px     Buttons, inputs
Medium         10px    Tabs
Large          12px    Cards, tables
Extra Large    16px    Login card, modals
Pill           20px    Workflow steps
```

---

## 💫 Shadow System

```
Subtle         rgba(0, 0, 0, 0.05), 10-12px blur
Standard       rgba(0, 0, 0, 0.08), 15px blur
Elevated       rgba(10, 61, 98, 0.25), 10px blur (Dark Blue)
Gold Accent    rgba(251, 197, 49, 0.3), 8-12px blur
```

---

## 🎬 Animation Guidelines

### Transitions
```
Fast           150ms   Button hover, small elements
Standard       250ms   Navigation, selection indicators
Slow           400ms   Module transitions, fades
```

### Easing
```
ease-in-out    Default for most transitions
ease-out       For appearing elements
ease-in        For disappearing elements
```

---

## 📱 Responsive Breakpoints

```
Desktop        1280px  Primary target
Tablet         1024px  Adjusted layouts
Mobile         768px   Simplified views
```

---

## 🎨 Usage Examples

### Dashboard Statistics
```java
VBox statCard = new VBox(8);
statCard.getStyleClass().add("stat-card");

Label value = new Label("1,234");
value.getStyleClass().add("stat-card-value");

Label title = new Label("Total Residents");
title.getStyleClass().add("stat-card-title");

statCard.getChildren().addAll(value, title);
```

### Workflow Step
```java
Label step = new Label("Approved");
step.getStyleClass().addAll("workflow-step", "workflow-step-completed");
```

### Priority Badge
```java
Label priority = new Label("HIGH");
priority.getStyleClass().add("priority-high");
```

### Action Button
```java
Button action = new Button("Process Document");
action.getStyleClass().add("button-primary");
```

### Captain's Action
```java
VBox captainCard = new VBox(10);
captainCard.getStyleClass().add("captain-action-card");
captainCard.setOnMouseClicked(e -> handleApproval());
```

---

## 🔍 Finding Components

### By Module
- **Dashboard**: `.stat-card`, `.quick-action-card`, `.trend-up/down`
- **Residents**: `.table-view`, `.form-section`, `.input-standard`
- **Certificates**: `.workflow-step-*`, `.badge-*`
- **Complaints**: `.priority-*`, `.upload-zone`, `.news-card`
- **Announcements**: `.news-card`, `.impact-*`
- **Financial**: `.progress-bar`, `.badge-gold`
- **Security**: `.security-indicator`, `.encryption-badge`

### By Function
- **Navigation**: `.sidebar-button`, `.submenu-button`
- **Actions**: `.button-primary/secondary/tertiary/danger/accent`
- **Status**: `.badge-*`, `.workflow-step-*`, `.priority-*`
- **Data Display**: `.table-view`, `.stat-card`, `.content-box`
- **Input**: `.text-field`, `.input-standard`, `.form-label`

---

## 🎯 Design Principles

1. **Card-Based Architecture**
   - Everything in elevated cards
   - Consistent shadows and borders
   - Clear visual hierarchy

2. **Surgical Borders**
   - 1px borders in slate color
   - Separate without cluttering
   - Maintain clean aesthetics

3. **High Contrast**
   - Dark blue for authority
   - Gold for leadership
   - White for clarity

4. **Professional Typography**
   - Outfit for impact
   - Inter for readability
   - Bold weights for hierarchy

5. **Semantic Colors**
   - Green = Success/Complete
   - Orange = Warning/Pending
   - Red = Error/Critical
   - Blue = Info/Neutral
   - Gold = Leadership/Special

---

## 🚀 Quick Start Checklist

- [x] Color palette updated to Dark Blue + Gold
- [x] Typography system implemented (Outfit + Inter)
- [x] Sidebar redesigned with dark blue gradient
- [x] Buttons enhanced with shadows and hover effects
- [x] Cards elevated with surgical borders
- [x] Tables optimized for high-density data
- [x] Badges made semi-transparent with borders
- [x] Workflow tracking styles added
- [x] Priority indicators created
- [x] Security visualization styles added
- [x] Quick action cards implemented
- [x] Captain's action cards (gold) added

---

## 📞 Need Help?

**Finding a style:**
1. Check this quick reference
2. Search `light-theme.css` for the class name
3. Refer to `JAVAFX_TECHNICAL_DASHBOARD_IMPLEMENTATION.md`

**Customizing:**
1. Locate the class in `light-theme.css`
2. Modify the properties
3. Save and restart application

**Adding new components:**
1. Follow existing patterns
2. Use color palette variables
3. Maintain spacing consistency
4. Test with different content lengths

---

**Version:** 2.0 - JavaFX Technical Dashboard
**Status:** ✅ Production Ready
**Last Updated:** 2026-04-21
