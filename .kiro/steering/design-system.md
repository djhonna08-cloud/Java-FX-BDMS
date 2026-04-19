---
title: San Marino BDMS Design System
description: Official design system and styling guidelines for Barangay San Marino Information Management System
version: 1.0
inclusion: auto
tags: [design, ui, css, styling, theme]
---

# San Marino BDMS Design System

## 🎨 Official Color Palette

### Primary Colors (Barangay Council Approved)

```css
/* San Marino Blue - Peace, trust, professionalism */
--primary-blue: #446CAC;
--primary-blue-dark: #365589;
--primary-blue-light: #5A84C4;

/* Champagne Gold - Service, professionalism, leadership */
--primary-gold: #FBC531;
--primary-gold-dark: #E6B02D;
--primary-gold-light: #FCD45F;

/* Cloud Dancer - Clean and modern interface */
--primary-white: #F0EEE9;
--primary-white-dark: #E5E3DE;
--primary-white-light: #F8F7F4;
```

### Semantic Colors

```css
/* Success */
--success-color: #10b981;
--success-dark: #059669;
--success-light: #34d399;

/* Warning */
--warning-color: #f59e0b;
--warning-dark: #d97706;
--warning-light: #fbbf24;

/* Error */
--error-color: #ef4444;
--error-dark: #dc2626;
--error-light: #f87171;

/* Info */
--info-color: #3b82f6;
--info-dark: #2563eb;
--info-light: #60a5fa;
```

### Neutral Colors

```css
/* Dark Theme */
--dark-bg-primary: #020617;
--dark-bg-secondary: #1e293b;
--dark-bg-tertiary: #334155;
--dark-text-primary: #ffffff;
--dark-text-secondary: rgba(255,255,255,0.8);
--dark-text-tertiary: rgba(255,255,255,0.6);

/* Light Theme */
--light-bg-primary: #F0EEE9;  /* Cloud Dancer */
--light-bg-secondary: #ffffff;
--light-bg-tertiary: #f8fafc;
--light-text-primary: #1e293b;
--light-text-secondary: #64748b;
--light-text-tertiary: #94a3b8;
```

---

## 📝 Typography System

### Font Families

```css
/* Primary Font - System Default for Performance */
--font-primary: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;

/* Monospace Font - For Code/Data Display */
--font-mono: "Consolas", "Monaco", "Courier New", monospace;
```

### Font Sizes (Large & Readable)

```css
/* Headings */
--font-size-h1: 32px;      /* Page Titles */
--font-size-h2: 24px;      /* Section Titles */
--font-size-h3: 20px;      /* Subsection Titles */
--font-size-h4: 18px;      /* Card Titles */

/* Body Text */
--font-size-base: 16px;    /* Default body text */
--font-size-large: 18px;   /* Emphasized text */
--font-size-small: 14px;   /* Secondary text */
--font-size-xs: 12px;      /* Labels, captions */

/* UI Elements */
--font-size-button: 16px;  /* Buttons */
--font-size-input: 16px;   /* Form inputs */
--font-size-label: 14px;   /* Form labels */
```

### Font Weights

```css
--font-weight-light: 300;
--font-weight-normal: 400;
--font-weight-medium: 500;
--font-weight-semibold: 600;
--font-weight-bold: 700;
```

### Line Heights

```css
--line-height-tight: 1.2;
--line-height-normal: 1.5;
--line-height-relaxed: 1.75;
--line-height-loose: 2;
```

---

## 🎯 Component Styling Standards

### Buttons

```css
/* Primary Button - San Marino Blue */
.button-primary {
    -fx-background-color: linear-gradient(to right, #446CAC, #365589);
    -fx-text-fill: #ffffff;
    -fx-font-size: 16px;
    -fx-font-weight: 600;
    -fx-padding: 12px 24px;
    -fx-background-radius: 8px;
    -fx-cursor: hand;
    -fx-effect: dropshadow(gaussian, rgba(68, 108, 172, 0.3), 8, 0, 0, 3);
}

.button-primary:hover {
    -fx-background-color: linear-gradient(to right, #5A84C4, #446CAC);
    -fx-effect: dropshadow(gaussian, rgba(68, 108, 172, 0.5), 12, 0, 0, 4);
}

/* Secondary Button - Champagne Gold */
.button-secondary {
    -fx-background-color: #FBC531;
    -fx-text-fill: #1e293b;
    -fx-font-size: 16px;
    -fx-font-weight: 600;
    -fx-padding: 12px 24px;
    -fx-background-radius: 8px;
    -fx-cursor: hand;
}

.button-secondary:hover {
    -fx-background-color: #FCD45F;
}

/* Tertiary Button - Outline */
.button-tertiary {
    -fx-background-color: transparent;
    -fx-border-color: #446CAC;
    -fx-border-width: 2px;
    -fx-text-fill: #446CAC;
    -fx-font-size: 16px;
    -fx-font-weight: 600;
    -fx-padding: 10px 22px;
    -fx-background-radius: 8px;
    -fx-border-radius: 8px;
    -fx-cursor: hand;
}

/* Danger Button */
.button-danger {
    -fx-background-color: #ef4444;
    -fx-text-fill: #ffffff;
    -fx-font-size: 16px;
    -fx-font-weight: 600;
    -fx-padding: 12px 24px;
    -fx-background-radius: 8px;
    -fx-cursor: hand;
}
```

### Form Inputs

```css
.text-field, .text-area, .password-field {
    -fx-font-size: 16px;
    -fx-padding: 12px 16px;
    -fx-background-radius: 8px;
    -fx-border-radius: 8px;
    -fx-border-width: 2px;
}

/* Light Theme */
.text-field {
    -fx-background-color: #ffffff;
    -fx-border-color: #cbd5e1;
    -fx-text-fill: #1e293b;
}

.text-field:focused {
    -fx-border-color: #446CAC;
    -fx-effect: dropshadow(gaussian, rgba(68, 108, 172, 0.2), 8, 0, 0, 0);
}

/* Labels */
.form-label {
    -fx-font-size: 14px;
    -fx-font-weight: 600;
    -fx-text-fill: #1e293b;
}
```

### Cards

```css
.card {
    -fx-background-color: #ffffff;
    -fx-background-radius: 12px;
    -fx-border-color: #e2e8f0;
    -fx-border-width: 1px;
    -fx-border-radius: 12px;
    -fx-padding: 20px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);
}

.card-header {
    -fx-font-size: 20px;
    -fx-font-weight: 700;
    -fx-text-fill: #1e293b;
}

.card-body {
    -fx-font-size: 16px;
    -fx-text-fill: #64748b;
}
```

### Tables

```css
.table-view {
    -fx-background-color: #ffffff;
    -fx-background-radius: 12px;
    -fx-border-color: #e2e8f0;
    -fx-border-width: 1px;
    -fx-border-radius: 12px;
    -fx-padding: 8px;
}

.table-view .column-header .label {
    -fx-font-size: 14px;
    -fx-font-weight: 700;
    -fx-text-fill: #1e293b;
}

.table-view .table-cell {
    -fx-font-size: 15px;
    -fx-text-fill: #64748b;
    -fx-padding: 12px 8px;
}

.table-row-cell:selected {
    -fx-background-color: #446CAC;
}

.table-row-cell:selected .table-cell {
    -fx-text-fill: #ffffff;
}
```

### Navigation

```css
.sidebar {
    -fx-background-color: #446CAC;
    -fx-padding: 20px;
}

.sidebar-button {
    -fx-font-size: 16px;
    -fx-font-weight: 500;
    -fx-text-fill: rgba(255,255,255,0.8);
    -fx-padding: 12px 16px;
    -fx-background-radius: 8px;
    -fx-cursor: hand;
}

.sidebar-button:hover {
    -fx-background-color: rgba(255,255,255,0.1);
    -fx-text-fill: #ffffff;
}

.sidebar-button.selected {
    -fx-background-color: #FBC531;
    -fx-text-fill: #1e293b;
}

.sidebar-button .ikonli-font-icon {
    -fx-icon-size: 20px;
}
```

---

## 📐 Spacing System

```css
/* Consistent spacing scale */
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 12px;
--spacing-lg: 16px;
--spacing-xl: 20px;
--spacing-2xl: 24px;
--spacing-3xl: 32px;
--spacing-4xl: 40px;
--spacing-5xl: 48px;
```

### Padding Guidelines

- **Small components**: 8px - 12px
- **Medium components**: 16px - 20px
- **Large components**: 24px - 32px
- **Containers**: 32px - 48px

### Margin Guidelines

- **Between elements**: 12px - 16px
- **Between sections**: 24px - 32px
- **Between major sections**: 40px - 48px

---

## 🎭 Border Radius System

```css
--radius-sm: 4px;    /* Small elements */
--radius-md: 8px;    /* Buttons, inputs */
--radius-lg: 12px;   /* Cards, containers */
--radius-xl: 16px;   /* Large containers */
--radius-full: 9999px; /* Pills, circles */
```

---

## 🌓 Theme Implementation

### Dark Theme Colors

```css
.root {
    -fx-background-color: #020617;
    -fx-primary-color: #446CAC;
    -fx-secondary-color: #FBC531;
    -fx-accent-color: #FBC531;
    -fx-text-primary: #ffffff;
    -fx-text-secondary: rgba(255,255,255,0.8);
}
```

### Light Theme Colors

```css
.root {
    -fx-background-color: #F0EEE9;  /* Cloud Dancer */
    -fx-primary-color: #446CAC;
    -fx-secondary-color: #FBC531;
    -fx-accent-color: #446CAC;
    -fx-text-primary: #1e293b;
    -fx-text-secondary: #64748b;
}
```

---

## ✅ Design Consistency Checklist

When implementing or reviewing UI components, ensure:

### Colors
- [ ] Primary actions use San Marino Blue (#446CAC)
- [ ] Secondary actions use Champagne Gold (#FBC531)
- [ ] Background uses Cloud Dancer (#F0EEE9) in light theme
- [ ] Success states use green (#10b981)
- [ ] Error states use red (#ef4444)
- [ ] Warning states use amber (#f59e0b)

### Typography
- [ ] All body text is at least 16px
- [ ] Headings use appropriate hierarchy (32px, 24px, 20px, 18px)
- [ ] Font weights are consistent (400 for body, 600 for emphasis, 700 for headings)
- [ ] Line height is 1.5 for body text

### Spacing
- [ ] Padding follows 4px/8px grid system
- [ ] Consistent spacing between elements (12px-16px)
- [ ] Adequate whitespace around interactive elements

### Components
- [ ] Buttons have minimum 12px vertical padding
- [ ] Form inputs have 12px-16px padding
- [ ] Cards have 20px-24px padding
- [ ] Border radius is 8px for buttons/inputs, 12px for cards

### Accessibility
- [ ] Color contrast ratio meets WCAG AA standards (4.5:1 for text)
- [ ] Interactive elements have visible focus states
- [ ] Font sizes are readable (minimum 14px for UI, 16px for content)
- [ ] Touch targets are at least 44x44px

---

## 🔧 Implementation Guidelines

### When Creating New Components

1. **Start with the design system colors**
   - Use CSS variables from this guide
   - Never hardcode colors directly

2. **Follow typography scale**
   - Use predefined font sizes
   - Maintain consistent font weights

3. **Apply spacing consistently**
   - Use the spacing scale (4px increments)
   - Maintain visual rhythm

4. **Use semantic naming**
   - `.button-primary` not `.blue-button`
   - `.text-error` not `.red-text`

### When Updating Existing Components

1. **Audit current styling**
   - Check if colors match the palette
   - Verify font sizes are readable
   - Ensure spacing is consistent

2. **Refactor incrementally**
   - Update one component type at a time
   - Test in both light and dark themes
   - Verify accessibility

3. **Document changes**
   - Note any deviations from the system
   - Update this guide if new patterns emerge

---

## 🎨 Common Patterns

### Status Badges

```css
.badge {
    -fx-font-size: 12px;
    -fx-font-weight: 600;
    -fx-padding: 4px 12px;
    -fx-background-radius: 12px;
}

.badge-success {
    -fx-background-color: #d1fae5;
    -fx-text-fill: #065f46;
}

.badge-warning {
    -fx-background-color: #fef3c7;
    -fx-text-fill: #92400e;
}

.badge-error {
    -fx-background-color: #fee2e2;
    -fx-text-fill: #991b1b;
}

.badge-info {
    -fx-background-color: #dbeafe;
    -fx-text-fill: #1e40af;
}
```

### Notification Styles

```css
.notification {
    -fx-background-color: #ffffff;
    -fx-background-radius: 8px;
    -fx-padding: 16px;
    -fx-border-color: #e2e8f0;
    -fx-border-width: 1px;
    -fx-border-radius: 8px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);
}

.notification-success {
    -fx-border-color: #10b981;
    -fx-border-width: 2px 1px 1px 1px;
}

.notification-error {
    -fx-border-color: #ef4444;
    -fx-border-width: 2px 1px 1px 1px;
}
```

### Modal Dialogs

```css
.dialog-pane {
    -fx-background-color: #ffffff;
    -fx-background-radius: 12px;
    -fx-padding: 24px;
}

.dialog-pane .header-panel {
    -fx-font-size: 24px;
    -fx-font-weight: 700;
    -fx-text-fill: #1e293b;
}

.dialog-pane .content {
    -fx-font-size: 16px;
    -fx-text-fill: #64748b;
    -fx-padding: 16px 0;
}
```

---

## 🚀 Quick Reference

### Most Common Colors
- **Primary Action**: `#446CAC` (San Marino Blue)
- **Secondary Action**: `#FBC531` (Champagne Gold)
- **Background**: `#F0EEE9` (Cloud Dancer)
- **Success**: `#10b981`
- **Error**: `#ef4444`
- **Text Primary**: `#1e293b`
- **Text Secondary**: `#64748b`

### Most Common Font Sizes
- **Headings**: 32px, 24px, 20px, 18px
- **Body**: 16px
- **Small**: 14px
- **Tiny**: 12px

### Most Common Spacing
- **Small**: 8px, 12px
- **Medium**: 16px, 20px
- **Large**: 24px, 32px

### Most Common Radius
- **Buttons/Inputs**: 8px
- **Cards**: 12px
- **Pills**: 9999px

---

## 📚 Resources

### Color Contrast Checker
- Ensure text meets WCAG AA: 4.5:1 ratio
- Large text (18px+): 3:1 ratio minimum

### Font Size Guidelines
- Minimum for UI elements: 14px
- Minimum for body text: 16px
- Optimal for readability: 16px-18px

### Touch Target Size
- Minimum: 44x44px
- Recommended: 48x48px

---

**Last Updated**: April 19, 2026  
**Version**: 1.0  
**Status**: Official Design System

**Note**: This design system is based on the Barangay Council's approved theme and should be followed for all UI development to ensure consistency and professionalism across the application.
