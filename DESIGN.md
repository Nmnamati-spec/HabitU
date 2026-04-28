---
name: HabitU
description: Warm habit tracking and social accountability for young adults
colors:
  deep-bark: "#130F0B"
  warm-coal: "#1D1812"
  ember-card: "#252018"
  ember-border: "#332B1F"
  golden-hour: "#D4A832"
  tag-glow: "#261E0A"
  warm-cream: "#F2EDE6"
  warm-dust: "#7A7060"
  warm-haze: "#A09480"
  growth: "#3ECC7F"
  ember-signal: "#F07D3C"
  violet: "#7B86F4"
typography:
  display:
    fontFamily: "sans-serif-black, sans-serif"
    fontSize: "22sp"
    fontWeight: 900
    lineHeight: 1.1
  headline:
    fontFamily: "sans-serif, sans-serif"
    fontSize: "18sp"
    fontWeight: 700
    lineHeight: 1.2
  title:
    fontFamily: "sans-serif, sans-serif"
    fontSize: "14sp"
    fontWeight: 700
    lineHeight: 1.3
  body:
    fontFamily: "sans-serif, sans-serif"
    fontSize: "13sp"
    fontWeight: 400
    lineHeight: 1.5
  label:
    fontFamily: "sans-serif, sans-serif"
    fontSize: "11sp"
    fontWeight: 400
    lineHeight: 1.2
  micro:
    fontFamily: "sans-serif, sans-serif"
    fontSize: "9sp"
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: "0.07em"
rounded:
  pill: "22dp"
  card: "16dp"
  button: "10dp"
  chip: "8dp"
  tag: "6dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  xl: "24dp"
components:
  button-primary:
    backgroundColor: "{colors.golden-hour}"
    textColor: "{colors.deep-bark}"
    rounded: "{rounded.button}"
    padding: "14dp 24dp"
  button-ghost:
    backgroundColor: "{colors.ember-card}"
    textColor: "{colors.warm-cream}"
    rounded: "{rounded.button}"
    padding: "14dp 24dp"
  chip-active:
    backgroundColor: "{colors.golden-hour}"
    textColor: "{colors.deep-bark}"
    rounded: "{rounded.chip}"
    padding: "3dp 8dp"
  chip-inactive:
    backgroundColor: "{colors.ember-card}"
    textColor: "{colors.warm-dust}"
    rounded: "{rounded.chip}"
    padding: "3dp 8dp"
  card-community:
    backgroundColor: "{colors.ember-card}"
    rounded: "{rounded.card}"
    padding: "12dp"
  input-field:
    backgroundColor: "{colors.ember-card}"
    textColor: "{colors.warm-cream}"
    rounded: "{rounded.button}"
    padding: "12dp 16dp"
---

# Design System: HabitU

## 1. Overview

**Creative North Star: "The Warm Training Log"**

HabitU is a dark editorial journal, not a neon fitness app. The visual system is built from warm charcoal surfaces — deep bark blacks, amber card layers, ember borders — lit by a single signature accent the colour of golden-hour light caught mid-run. The feed feels like flipping through a beautifully kept record of who you are becoming: image-forward, varied, alive with real content. Scroll it and feel the pull of possibility, not the anxiety of a streak counter.

The system draws from two references held in tension. From Pinterest: image-first discovery, masonry rhythm, editorial hierarchy, the sense that every card was placed with intention. From Strava: earned achievement, athletic restraint, social proof without vanity metrics. Where Pinterest is warm and browsable, Strava is focused and direct. HabitU lives where those two impulses meet — the morning you log a run and then scroll your community feed to see what everyone else is building.

This system explicitly rejects: neon fitness app energy (neon on black is for nightclubs, not habit formation), corporate wellness softness (Calm's pastel quietude is the wrong register), gamification clichés (no Duolingo-style progress bars or streak-guilt notifications), and generic Material Design defaults (identical card grids, flat-list screens, same padding everywhere).

**Key Characteristics:**
- Warm dark surfaces — charcoal tinted toward brown, never cold blue-black
- One earned accent — amber-gold appears only where it means something
- Image-forward layouts with deliberate height variation (no identical card grids)
- Weight contrast over size contrast for typographic hierarchy
- Flat elevation expressed through tonal warmth, never drop shadows
- Motion that feels satisfying, not busy — press scales, entry fades, completion moments

---

## 2. Colors: The Ember Palette

A restrained dark system warmed from black toward brown, lit by a single earned accent. The palette reads warm at a glance without announcing itself. It should feel like the glow of a screen in a dark early morning — purposeful, focused, alive.

### Primary
- **Golden Hour** (#D4A832 / oklch(72% 0.17 78)): The signature accent. Amber-gold, warm as backlit achievement. Used on primary CTAs, logged/completed states, active tab indicators, and the FAB. It appears on ≤15% of any screen — its rarity is its authority.
- **Tag Glow** (#261E0A / oklch(14% 0.06 78)): The ambient presence of golden-hour — used as background tint for tags and transparent overlays over lime-tinted content. Ties the accent back into the surface layer.

### Secondary
- **Growth** (#3ECC7F): Positive completion state. Open community badges, streak confirmations, habit-logged affirmations. Never used decoratively.
- **Ember Signal** (#F07D3C): Attention and request state. Pending invites, required actions, requests. The warmth of this orange harmonises naturally with the amber palette.
- **Violet** (#7B86F4): Privileged or special state. Admin badges, premium features, community moderator indicators.

### Neutral
- **Deep Bark** (#130F0B): The foundation. App background. So dark it reads almost black but carries faint warmth. Never use pure `#000000`.
- **Warm Coal** (#1D1812): Secondary surface layer. Bottom sheets, navigation backgrounds, modal backdrops.
- **Ember Card** (#252018): Card surfaces and input fields. The primary container colour.
- **Ember Border** (#332B1F): Dividers, card strokes, input outlines. Visible only in context — not decorative.
- **Warm Cream** (#F2EDE6): Primary text. Not pure white. The warmth keeps the contrast from feeling clinical.
- **Warm Dust** (#7A7060): Muted and secondary text. Dates, member counts, supporting labels.
- **Warm Haze** (#A09480): Mid-emphasis text and inactive icon states. Bridges cream and dust.

### Named Rules
**The Golden Hour Rule.** The amber accent is used exclusively on states that have been earned or chosen: a logged habit, a tapped CTA, an active tab. It never appears as decoration. If a surface is using golden-hour and nothing has been earned or activated, the colour is wrong.

**The Cold-Free Rule.** No pure blacks, no cool greys, no blue-shifted neutrals outside the semantic violet. Every neutral is tested against warm-coal — if it reads cooler, it is replaced. The existing `card_dark` (#161929) and `text_dim` (#9CA0CC) are both cold and should be phased out in favour of ember-card and warm-haze.

---

## 3. Typography

**Display Font:** sans-serif-black (Android system, weight 900)
**Body Font:** sans-serif (Android system, weight 400/700)
**Micro/Label Font:** sans-serif (Android system, weight 700, tracked out)

**Character:** The pairing relies on weight contrast rather than type family contrast. The system font at heavy weight reads architectural and confident — like editorial headers in a high-production magazine, not the rounded-soft style of a wellness app. Body text is set loose with generous line-height to give content room to breathe.

### Hierarchy
- **Display** (weight 900, 22sp, line-height 1.1): Screen titles. "HabitU", "My Habits". Set tight and heavy. One per screen.
- **Headline** (weight 700, 18sp, line-height 1.2): Section headers and modal titles. Used when a block of content needs a named anchor.
- **Title** (weight 700, 14sp, line-height 1.3): Card titles, community names, list item labels. The primary hierarchy within a card.
- **Body** (weight 400, 13sp, line-height 1.5): Description text, post content, input values. Never exceed 60 characters per line.
- **Label** (weight 400, 11sp, line-height 1.2): Member counts, dates, supporting metadata. Warm-dust colour by default.
- **Micro** (weight 700, 9sp, line-height 1.1, letter-spacing 0.07em): Category chips, status badges. All caps or tracked bold only. Never use at body size.

### Named Rules
**The Weight Gap Rule.** Adjacent text roles must differ by at least 200 weight points or 3sp in size — never both the same weight and close in size. A 700-weight title followed by a 700-weight label is invisible hierarchy. Use warm-dust or warm-haze to create role distinction when size and weight alone are insufficient.

**The Micro Exception.** The 9sp micro size is valid only in chips, tags, and status badges. Anything that needs to be read at length must be at minimum 13sp. Never shrink body copy to fit a container — reflow the layout instead.

---

## 4. Elevation

HabitU is flat by design. No drop shadows. Depth is expressed through tonal warmth: deeper surfaces are darker and cooler; foreground content sits on warmer, lighter ember layers. The hierarchy reads bottom-up: deep-bark → warm-coal → ember-card → warm-cream content.

The one exception is press state. MaterialCardView components use the `scale_press` animator (0.97 scale over 120ms, restore over 200ms, both decelerate_quad). This tactile depression is the only physical metaphor in the system — it replaces the elevation change that would otherwise indicate depth.

### Named Rules
**The Flat-By-Default Rule.** cardElevation is 0dp everywhere. If a component needs to feel elevated, the solution is a warmer background, not a shadow. Drop shadows that appear outside of the press state are wrong.

**The Tonal Depth Rule.** Surface stack from dark to light: deep-bark (background) → warm-coal (sheets, overlays) → ember-card (cards, inputs) → warm-cream (content). Never invert this order. A card cannot sit on a surface lighter than itself.

---

## 5. Components

### Buttons
Buttons are direct and confident — no gradients, no glows, no rounded-full pill shapes except for explicit pill/tag components.

- **Shape:** Gently curved (10dp radius)
- **Primary (btn-primary):** Golden-hour fill (#D4A832), deep-bark text (#130F0B), 14dp vertical / 24dp horizontal padding. The text-on-amber contrast is the visual commitment of action.
- **Ghost (btn-ghost):** Ember-card fill (#252018), warm-cream text, ember-border stroke (1dp). Used for secondary actions within screens.
- **Press state:** scale_press animator (0.97 over 120ms). Gives the button a satisfying physical click without elevation changes.
- **Typography:** 13sp, weight 700. No all-caps unless in micro role.

### Chips and Tags
- **Active chip:** Golden-hour fill, deep-bark text, 8dp radius, 3dp/8dp padding. Used for selected filter states, active tab toggles.
- **Inactive chip:** Ember-card fill, warm-dust text, same radius and padding. Never border-only chips — they read too sparse against warm dark surfaces.
- **Tags (content labels):** Tag-glow fill (#261E0A, 12% amber tint), warm-cream text, 6dp radius. The glow tint ties tag backgrounds to the accent without using the full accent colour.

### Cards
Cards are the primary layout unit on the feed and communities screens. They must never be identical in height — the Pinterest reference demands variance in the vertical rhythm.

- **Corner Style:** Gently curved (16dp radius — warm, not aggressive)
- **Background:** Ember-card (#252018)
- **Shadow:** None (cardElevation 0dp — see Elevation)
- **Border:** 1dp ember-border (#332B1F) stroke. Structural, not decorative — it separates card from background without shadow.
- **Internal Padding:** 12dp standard. Thumbnail areas bleed to the card edge with no padding.
- **Press state:** scale_press animator applied via stateListAnimator on MaterialCardView.
- **Ripple:** #14FFFFFF (translucent warm white) — preserves the warm surface instead of a cold ripple.

### Feed Cards (Signature Component)
The feed is a mood board, not a timeline. Cards vary in height based on image aspect ratio. No two adjacent cards should share the same height.

- **Image area:** Full-bleed, card-edge to card-edge, with a 16dp radius clipped top. Image aspect ratios vary: 4:5, 1:1, and 3:4 are all valid. Crop to fill — never letterbox.
- **Metadata overlay:** A bottom scrim (warm to transparent, 48dp tall) overlays the image. Username and action icons sit in this scrim. White text only.
- **Caption (if present):** Below the image, inside the card. Body size (13sp), warm-cream, 12dp padding, max 3 lines before truncation.
- **Engagement row:** Label-size (11sp), warm-dust, icon + count pairs. Horizontal, tight spacing.

### Inputs / Fields
- **Style:** Ember-card fill (#252018), ember-border stroke (1dp), 10dp radius. No underline style — the filled style reads warmer and more contained.
- **Focus treatment:** Ember-border shifts to golden-hour (#D4A832) at 1dp. No glow, no shadow — the colour change is the signal.
- **Placeholder text:** Warm-dust (#7A7060). Never warm-haze — the contrast must be distinct from the active text.
- **Error state:** Ember-signal (#F07D3C) border, error message in label size below the field.

### Bottom Navigation
- **Background:** Deep-bark (#130F0B), seamlessly blended with the app background.
- **Active icon/label:** Golden-hour (#D4A832). The one place golden-hour appears without an explicit user action — the active tab is the user's current location, which is an earned state.
- **Inactive icon/label:** Warm-dust (#7A7060).
- **No indicator pill or selected background** — the colour change alone carries the state. Additional indicators are visual clutter.

### Community Cards (Grid Layout)
- **Layout:** 2-column grid, 5dp gap, cards stretch to fill column width.
- **Thumbnail:** Square (1:1), full-bleed, community-thumb-bg as fallback (#0C1E35 — this is the one cold surface that remains, as community thumbnails are often user-generated images in dark contexts).
- **Category chip:** Micro size (9sp, tracked), sits over the thumbnail at bottom-left. Never more than one chip per card.
- **Action button:** Ghost style, 11sp bold, within card info section.

---

## 6. Do's and Don'ts

### Do:
- **Do** warm every neutral toward brown, not blue. If a surface reads cooler than ember-card, replace it.
- **Do** vary feed card heights deliberately. The Pinterest reference demands it — identical-height cards betray the system.
- **Do** use golden-hour only on earned or active states. Its power comes from restraint.
- **Do** use weight contrast (900 vs 400) as the primary hierarchy tool, then colour, then size.
- **Do** let images bleed to card edges on feed cards. Padding around images kills editorial energy.
- **Do** apply scale_press (0.97, 120ms) to every tappable card and button. Tactile feedback is the only motion that is always present.
- **Do** use ember-signal (#F07D3C) for attention states — it harmonises naturally with the warm palette and is visually distinct without being cold.
- **Do** test new screens against the Cold-Free Rule: no surface, text, or border should read cooler than the warm-coal layer.

### Don't:
- **Don't** use neon or high-chroma lime green as a primary accent. The previous #C8F53B read cold and electric — exactly the neon fitness energy the system rejects. Golden-hour (#D4A832) is the evolved form.
- **Don't** use `#000000` or `#FFFFFF`. Both read clinical and cold. Use deep-bark and warm-cream respectively.
- **Don't** build identical card grids. If every card is the same size, it is a list in disguise. Vary height, vary image aspect ratio, vary content density.
- **Don't** use border-left or border-right greater than 1dp as a coloured accent stripe. This is a banned pattern system-wide — rewrite with background tints, leading icons, or full borders.
- **Don't** apply gradient text (`background-clip: text` with a gradient). Single solid colour only — emphasis through weight, not gradients.
- **Don't** reach for modals when an inline expansion, bottom sheet, or in-place reveal works. Modals interrupt the session flow that makes this app feel warm.
- **Don't** use the corporate wellness palette (Calm/Headspace-style soft pastels, muted teals, clinical whites). The wrong register entirely.
- **Don't** gamify with Duolingo-style mechanics (progress bars with faces, streak-guilt countdowns, level-up pop-ups). Achievement should feel like a fist-bump, not a badge ceremony.
- **Don't** use text_dim (#9CA0CC) or card_dark (#161929) — these are legacy cold-palette values, scheduled for removal. Use warm-haze and ember-card respectively.
- **Don't** put golden-hour on decorative elements. If the amber appears on something the user didn't cause or hasn't earned, the Golden Hour Rule is broken.
