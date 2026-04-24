# Design System Quick Reference Guide

## 🎨 Barangay San Marino BDMS - Design Standards

Quick reference for developers implementing UI components.

---

## 📏 Spacing Scale (8px Grid)

Use these values for ALL spacing (padding, margin, gaps):

```
4px  → Micro spacing (tight groups)
8px  → Small spacing (related items)
12px → Medium spacing (form fields, cards)
16px → Standard spacing (sections)
20px → Large spacing (major sections)
24px → XL spacing (page divisions)
32px → XXL spacing (major breaks)
```

**Examples:**
```css
.tight-group    { -fx-spacing: 4px; }
.form-field     { -fx-spacing: 12px; }
.section        { -fx-padding: 16px; }
.page-section   { -fx-padding: 24px; }
```

---

## 🔤 Typography Scale

| Level | Size | Weight | Use Case |
|-------|------|--------|----------|
| **Display Large** | 32px | 700 | Hero headings |
| **Display** | 28px | 700 | Page titles |
| **Panel Title** | 20px | 700 | Major sections |
| **Section** | 18px | 700 | Subsections |
| **Card Title** | 16px | 700 | Card headers |
| **Body** | 14px | 400 | Standard text |
| **Small** | 13px | 400 | Secondary text |
| **Micro** | 11px | 400 | Metadata, badges |

**CSS Classes:**
```css
.display-heading     /* 28px, bold */
.section-heading     /* 20px, bold */
.panel-title         /* 20px, bold */
.card-title          /* 18px, bold */
.text-body           /* 14px, normal */
.text-body-sm        /* 13px, normal */
.text-meta           /* 12px, muted */
.text-micro          /* 11px, muted */
```

---

## 🔘 Button Sizes

| Size | Height | Padding | Font | Min Width |
|------|--------|---------|------|-----------|
| **Small** | 32px | 8px 16px | 13px | 80px |
| **Standard** | 40px | 12px 24px | 15px | 100px |
| **Large** | 48px | 14px 28px | 16px | 120px |

**Usage:**
```java
button.getStyleClass().addAll("button-primary", "button-small");
button.getStyleClass().addAll("button-secondary", "button-standard");
button.getStyleClass().addAll("button-danger", "button-large");
```

**Button Variants:**
- `.button-primary` - Dark blue (authority)
- `.button-secondary` - Gold (leadership)
- `.button-tertiary` - Outlined (subtle)
- `.button-danger` - Red (critical)
- `.button-success` - Green (positive)
- `.button-warning` - Orange (caution)
- `.button-info` - Blue (informational)

---

## 📝 Form Inputs

| Size | Height | Padding | Font |
|------|--------|---------|------|
| **Small** | 36px | 8px 12px | 13px |
| **Standard** | 44px | 12px 16px | 15px |
| **Large** | 48px | 14px 16px | 16px |

**CSS Classes:**
```css
.input-sm        /* 36px height */
.input-standard  /* 44px height (default) */
.input-lg        /* 48px height */
```

**All inputs include:**
- 8px border radius
- 1px border (#e2e8f0)
- 2px focus border (#0A3D62)
- Subtle focus shadow

---

## 🃏 Card System

| Type | Padding | Radius | Shadow | Use Case |
|------|---------|--------|--------|----------|
| **Small** | 16px | 8px | Level 1 | Compact cards |
| **Standard** | 20px | 12px | Level 2 | Default cards |
| **Large** | 24px | 12px | Level 3 | Important cards |

**CSS Classes:**
```css
.card-sm    /* Small card */
.card       /* Standard card */
.card-lg    /* Large card */
```

**Semantic Variants:**
```css
.card-info     /* Blue info card */
.card-success  /* Green success card */
.card-warning  /* Orange warning card */
.card-surface  /* Gray surface card */
```

---

## 📊 Table Styling

**Standard Table:**
```css
Header:  12px padding, 14px font, bold
Cell:    10px padding, 13px font, normal
Row:     40px height
```

**Enhanced Table with Search:**
```css
Search Bar:  12px 16px padding, 36px height
Table:       16px padding
```

**Usage:**
```java
table.getStyleClass().add("table-view");
// or
table.getStyleClass().add("enhanced-table-container");
```

---

## 🎭 Shadow Hierarchy

| Level | Blur | Offset | Opacity | Use Case |
|-------|------|--------|---------|----------|
| **1** | 8px | 2px | 0.05 | Subtle elevation |
| **2** | 12px | 4px | 0.08 | Standard cards |
| **3** | 16px | 6px | 0.12 | Elevated elements |
| **4** | 20px | 8px | 0.15 | Modals, popovers |

**CSS:**
```css
Level 1: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2)
Level 2: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4)
Level 3: dropshadow(gaussian, rgba(0,0,0,0.12), 16, 0, 0, 6)
Level 4: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8)
```

---

## 🎨 Color Palette

### Primary Colors
```
Primary:   #0A3D62  (Dark Blue - Authority)
Secondary: #FBC531  (Gold - Leadership)
Surface:   #FFFFFF  (White - Clean)
Background:#F8FAFC  (Light Gray - Subtle)
```

### Semantic Colors
```
Success:   #10b981  (Green)
Warning:   #f59e0b  (Orange)
Error:     #ef4444  (Red)
Info:      #3b82f6  (Blue)
```

### Text Colors
```
Primary:   #0f172a  (Dark - Headings)
Secondary: #475569  (Medium - Body)
Tertiary:  #94a3b8  (Light - Meta)
```

### Border & Dividers
```
Border:    #e2e8f0  (Light Gray)
```

---

## 🔲 Border Radius

| Size | Value | Use Case |
|------|-------|----------|
| **Small** | 6px | Badges, pills |
| **Base** | 8px | Buttons, inputs |
| **Medium** | 10px | Medium cards |
| **Large** | 12px | Large cards, tables |
| **XL** | 16px | Modals, special |

---

## 🏷️ Badges

**Sizes:**
```css
.badge     /* 6px 12px padding, 11px font */
.badge-sm  /* 4px 8px padding, 10px font */
.badge-lg  /* 8px 16px padding, 12px font */
```

**Variants:**
```css
.badge-success  /* Green */
.badge-warning  /* Orange */
.badge-error    /* Red */
.badge-info     /* Blue */
.badge-gold     /* Gold */
```

---

## 📐 Layout Containers

**Standard Padding:**
```css
.content-panel     { -fx-padding: 24px; -fx-spacing: 20px; }
.form-section      { -fx-padding: 24px; -fx-spacing: 16px; }
.panel-header      { -fx-padding: 0 0 16px 0; }
```

**Spacing:**
```css
.section-spacing    { -fx-spacing: 16px; }
.section-spacing-lg { -fx-spacing: 24px; }
```

---

## 🎯 Common Patterns

### Form Layout
```java
VBox form = new VBox(12);  // 12px spacing
form.setPadding(new Insets(24));  // 24px padding
form.getStyleClass().add("form-section");
```

### Button Group
```java
HBox buttons = new HBox(8);  // 8px spacing
buttons.getStyleClass().add("action-button-container");
```

### Card with Title
```java
VBox card = new VBox(16);  // 16px spacing
card.setPadding(new Insets(20));  // 20px padding
card.getStyleClass().add("card");

Label title = new Label("Card Title");
title.getStyleClass().add("card-title");
```

### Table with Actions
```java
VBox container = new VBox(12);  // 12px spacing
container.getChildren().addAll(table, buttonBar);
```

---

## ✅ Quick Checklist

When creating a new component, ask:

- [ ] Does spacing follow 8px grid?
- [ ] Is font size from the standard scale?
- [ ] Are button sizes consistent?
- [ ] Do shadows match the hierarchy?
- [ ] Is border radius appropriate?
- [ ] Are colors from the palette?
- [ ] Is padding consistent with similar components?
- [ ] Does it align with existing patterns?

---

## 🚫 Common Mistakes to Avoid

❌ **Don't:**
- Use random spacing values (5px, 7px, 9px, 11px, 15px)
- Create custom font sizes
- Mix button heights
- Use inline styles for spacing
- Create one-off shadow values
- Ignore the 8px grid

✅ **Do:**
- Use spacing scale values
- Use typography scale
- Use standard button sizes
- Use CSS classes
- Use shadow hierarchy
- Follow the 8px grid

---

## 📚 Resources

- **Full Design Plan**: `DESIGN_OPTIMIZATION_PLAN.md`
- **Optimization Summary**: `CSS_OPTIMIZATION_SUMMARY.md`
- **Before/After**: `BEFORE_AFTER_COMPARISON.md`
- **Layout Analysis**: `LAYOUT_SPACE_ANALYSIS.md`
- **CSS File**: `src/main/resources/com/example/light-theme.css`

---

## 💡 Pro Tips

1. **Use VBox/HBox spacing** instead of manual padding
2. **Leverage CSS classes** instead of inline styles
3. **Follow the grid** - if unsure, round to nearest 8px
4. **Be consistent** - check similar components first
5. **Test thoroughly** - verify alignment and spacing

---

**Remember**: Consistency creates professionalism! 🎨✨
