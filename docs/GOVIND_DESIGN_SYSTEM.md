# GOVIND Design System
## Extracted from Stitch + Tailwind Configuration

> **Source of Truth:** Google Stitch project `projects/13868558863199017635`
> **Design Language:** Govind Expressive Material (M3-based, customized)
> **Font Family:** Plus Jakarta Sans (all weights: 400, 500, 600, 700, 800)

---

## 1. COLOR SYSTEM

### Primary / Brand
| Token | Hex | Usage |
|---|---|---|
| `primary` | `#002D11` | Deep brand — text, icons, contrast backgrounds |
| `primary-container` | `#064520` | **Main CTA backgrounds**, top bar, selected pill bg, FABs |
| `on-primary` | `#FFFFFF` | Text/icons on primary |
| `on-primary-container` | `#78B383` | Text/icons on primary-container |
| `primary-fixed` | `#B4F1BD` | Subtle brand tint bg |
| `primary-fixed-dim` | `#98D5A2` | Muted brand tint |

### Secondary / Fresh Green
| Token | Hex | Usage |
|---|---|---|
| `secondary` | `#246C18` | Section icons, links, "See All", category prices |
| `secondary-container` | `#A7F690` | Badges bg, hero pills, pulse indicators |
| `on-secondary` | `#FFFFFF` | Text on secondary |
| `on-secondary-container` | `#2B731E` | Text on secondary-container |

### Tertiary / Kitchen Orange-Red
| Token | Hex | Usage |
|---|---|---|
| `tertiary` | `#4D0E00` | Deep kitchen brand (unused directly on surface) |
| `tertiary-container` | `#741900` | Kitchen accent dark |
| `on-tertiary-container` | `#FF815F` | Kitchen accent bright text |
| `tertiary-fixed` | `#FFDBD1` | Kitchen tint bg |
| `tertiary-fixed-dim` | `#FFB5A1` | Kitchen tint muted |

### Surfaces
| Token | Hex | Usage |
|---|---|---|
| `surface` | `#F6FBF4` | Page background |
| `surface-dim` | `#D7DBD5` | Dimmed surface |
| `surface-bright` | `#F6FBF4` | Same as surface |
| `surface-container-lowest` | `#FFFFFF` | Card bg, search bar bg, product card bg |
| `surface-container-low` | `#F0F5EE` | Unselected pill bg |
| `surface-container` | `#EBEFE8` | General container |
| `surface-container-high` | `#E5E9E3` | Experience switcher track, cart badge bg |
| `surface-container-highest` | `#DFE4DD` | Highest elevated container |
| `surface-variant` | `#DFE4DD` | Variant bg |
| `on-surface` | `#181D19` | Primary text |
| `on-surface-variant` | `#414941` | Secondary / muted text |
| `background` | `#F6FBF4` | App background |

### Outline / Border
| Token | Hex | Usage |
|---|---|---|
| `outline` | `#717970` | Strong outline |
| `outline-variant` | `#C0C9BE` | Light outline, dividers, switcher border |

### Error
| Token | Hex | Usage |
|---|---|---|
| `error` | `#BA1A1A` | Error text/icons |
| `error-container` | `#FFDAD6` | Error background |
| `on-error` | `#FFFFFF` | Text on error |
| `on-error-container` | `#93000A` | Text on error-container |

### Inverse (Dark Surface)
| Token | Hex | Usage |
|---|---|---|
| `inverse-surface` | `#2D322D` | Floating dock bg |
| `inverse-on-surface` | `#EDF2EB` | Text on dark dock |
| `inverse-primary` | `#98D5A2` | Primary on dark bg |

---

## 2. TYPOGRAPHY SCALE

**All typefaces: Plus Jakarta Sans**

| Token | Size | LineHeight | Weight | LetterSpacing | Compose |
|---|---|---|---|---|---|
| `headline-xl` | 36sp | 44sp | 800 | -0.03em | `headlineLarge` (override) |
| `headline-xl-mobile` | 28sp | 34sp | 800 | -0.02em | Custom `headlineXlMobile` |
| `headline-lg` | 30sp | 38sp | 700 | -0.02em | Custom (not M3 default) |
| `headline-lg-mobile` | 22sp | 28sp | 700 | -0.01em | `headlineMedium` (override) |
| `headline-md` | 18sp | 24sp | 700 | -0.01em | `titleLarge` (override) |
| `headline-sm` | 16sp | 22sp | 600 | 0 | `titleMedium` (override) |
| `body-lg` | 16sp | 24sp | 400 | 0 | `bodyLarge` |
| `body-md` | 14sp | 20sp | 400 | 0 | `bodyMedium` |
| `body-sm` | 12sp | 16sp | 400 | 0 | `bodySmall` |
| `label-lg` | 14sp | 18sp | 700 | 0.01em | `labelLarge` |
| `label-md` | 12sp | 16sp | 600 | 0.02em | `labelMedium` |
| `label-sm` | 10sp | 12sp | 700 | 0.04em | `labelSmall` |
| `price-display` | 18sp | 22sp | 800 | -0.02em | Custom `priceDisplay` |
| `price-strikethrough` | 12sp | 16sp | 500 | 0 | Custom `priceStrikethrough` |

---

## 3. SPACING TOKENS

| Token | Value | Usage |
|---|---|---|
| `space-xs` | 4dp | Minimal gap |
| `space-sm` | 8dp | Small gap |
| `space-md` | 12dp | Medium gap |
| `space-lg` | 16dp | Section gap |
| `space-xl` | 24dp | Large section gap |
| `margin` | 16dp | Screen horizontal margin (mobile) |
| `margin-desktop` | 32dp | Screen horizontal margin (desktop) |
| `gutter` | 12dp | Grid gutter (mobile) |
| `gutter-desktop` | 24dp | Grid gutter (desktop) |

---

## 4. BORDER RADIUS

| Token | Value | Usage |
|---|---|---|
| `default` | 4dp | Small chips, badges |
| `lg` | 8dp | Buttons, small cards |
| `xl` | 12dp | Cards, containers |
| `2xl` | 16dp | Large cards, hero banners |
| `full` | 9999dp | Pills, circles, switcher |

---

## 5. COMPONENT SPECIFICATIONS

### 5.1 Top Header Bar
- **Background:** `surface/90` with blur (`backdrop-blur-xl`)
- **Shadow:** `0 1px 8px rgba(0,0,0,0.04)`
- **Layout:** Logo + Location + ETA pill + Cart icon + Profile avatar
- **Logo:** GOVIND brand image, `h-8`
- **Location:** `label-sm` "GOVIND EXPRESS" + `headline-sm` "Sector 48, Gurugram" with expand_more
- **ETA Pill:** `bg-secondary-container/40`, border `secondary/20`, rounded-full, ⚡ "12m"
- **Cart Badge:** `bg-surface-container-high/60`, material icon `shopping_cart`, count badge `bg-secondary`
- **Profile:** 32dp circle image

### 5.2 Experience Switcher
- **Track:** `bg-surface-container-high/70`, `rounded-full`, `shadow-inner`, border `outline-variant/30`
- **Active Pill:** `bg-primary-container`, white text, `shadow-[0_2px_8px_rgba(6,69,32,0.2)]`, Material Icon
- **Inactive Pill:** `bg-surface-container-low`, `text-on-surface-variant`, `font-semibold`
- **Min Height:** 44dp per pill
- **Icons:** `eco` (Fresh), `skillet` (Kitchen), `inventory_2` (Wholesale)
- **Active indicator:** Small green dot on active tab

### 5.3 Search Bar
- **Background:** `surface-container-lowest` (white)
- **Shape:** `rounded-full` (pill)
- **Shadow:** `0 4px 16px rgba(6,69,32,0.06)`
- **Icon:** `search` in `secondary` color
- **Placeholder:** "Search farm fresh vegetables, fruits, dairy..."
- **Right actions:** Voice mic + QR scanner icons

### 5.4 Filter Pills
- **Active:** `bg-primary-container`, white text, `check_circle` icon
- **Inactive:** `bg-surface-container-lowest`, `text-on-surface`
- **Shape:** `rounded-full`

### 5.5 Hero Banner
- **Shape:** `rounded-2xl` with overflow hidden
- **Height:** ~44dp (h-44)
- **Background:** Full-bleed product image
- **Overlay:** Gradient from-primary via-primary/80 to-transparent
- **Badges:** "Mandi Harvest 5 AM" pill, "10-15m" timer pill
- **Headline:** `headline-lg-mobile` (22sp/700) in white
- **CTA:** "Explore →" button in `bg-secondary` `rounded-full`

### 5.6 Category Grid
- **Layout:** 4-column grid, `gap-2.5`
- **Item:** Vertical stack: circle image (40dp in 52dp circle bg) + label + starting price
- **Circle background:** `secondary-container/30` or `tertiary-fixed/40`
- **Label:** `label-sm`, `font-bold`
- **Price hint:** `price-strikethrough` 10sp, `text-secondary`

### 5.7 Product Card (Fresh)
- **Shape:** `rounded-xl`
- **Background:** `surface-container-lowest`
- **Shadow:** `0 2px 10px rgba(0,0,0,0.06)` + `0 1px 3px rgba(0,0,0,0.04)`
- **Image:** `rounded-lg`, h-28, object-cover
- **Veg Indicator:** 6dp green circle
- **Title:** `headline-sm` (16sp/600), clamp-2
- **Unit:** `body-sm`, `text-on-surface-variant`
- **Price:** `price-display` (18sp/800), with MRP strikethrough
- **Discount badge:** `bg-error` absolute positioned, "24% OFF"
- **ADD button:** `bg-secondary`, white text, `label-md`, `rounded-full`, min-h 32dp
- **When in cart:** Replace ADD with quantity stepper

### 5.8 Floating Cart Dock
- **Position:** Fixed bottom, full width
- **Background:** `bg-inverse-surface`
- **Shape:** `rounded-full`
- **Layout:** Cart icon + item count + total + "View Cart →"
- **Text colors:** `inverse-on-surface`, `inverse-primary`
- **Shadow:** `0 -2px 20px rgba(0,0,0,0.15)`

### 5.9 Section Header
- **Layout:** Icon + Title + "See All" link
- **Title:** `headline-md` (18sp/700)
- **Icon:** Material symbol in `secondary`
- **Link:** `label-md` (12sp/600) in `secondary`

---

## 6. SCREEN LAYOUT SPECIFICATIONS

### Fresh Home (top to bottom)
1. Header Bar (fixed)
2. Experience Switcher
3. Search Bar (pill, voice + scan)
4. Express Filter Pills (horizontal scroll)
5. Hero Freshness Banner ("Harvested Today")
6. Hero Product Banner (full-bleed image + gradient)
7. Category Grid (4×2)
8. "Daily Harvest Picks" product carousel
9. "Deals of the Day" section
10. "Daily Breakfast Basket" combo card
11. Floating Cart Dock (fixed bottom)
12. Bottom Navigation (fixed bottom, behind dock)

### Kitchen Home
1. Kitchen-themed header (orange accent)
2. Experience Switcher
3. "Order fresh & healthy" hero
4. Kitchen Basket summary card
5. Category filter chips
6. Menu items list
7. Category shortcuts grid
8. Floating Cart Dock

### Wholesale Home
1. Wholesale-themed header (deep green / navy)
2. Experience Switcher
3. Wholesale hero & identity
4. Category grid
5. Bulk tier explanation banner
6. Product list with tier pricing
7. WhatsApp / Contact CTAs
8. Floating Cart Dock
