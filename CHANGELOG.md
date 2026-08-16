# nixStats — Changelog

A configurable statistics sidebar HUD.
Client-side only.

Format based on [Keep a Changelog](https://keepachangelog.com/); versioning per [SemVer](https://semver.org/).

## [1.4.3] — 2026-08-16

Three-state name display, and per-column width controls.

### Added
- **Show Names / Show Abbrev / Show None** — the config screen's name button now cycles three states instead of two, and its caption always reads the mode in effect. **Show Names** draws the full label (*Stone Mined*), **Show Abbrev** keeps only the action (*Mined*), and **Show None** drops the label entirely so a row is just its icon and its number. Under Show None the same block listed under several categories is indistinguishable — that ambiguity is intended, and the icon plus the count is the point.
- **Per-column width controls** — the single `Pad` spinner became three, and the icon gap became adjustable: **IGap** (icon → label), **LPad** (label column) and **VPad** (value column), each `0`–`20`. Columns still auto-fit their content; these tune the breathing room around it.
- **ECol** — the width the middle column takes when *no* row populates it, `0`–`20`, default `0`. With every row shedding its name under Show None, the column now collapses completely and the icons sit against the numbers. As soon as one row keeps a label — a Phantom or advancement row — the column auto-fits that instead and ECol is ignored.

### Changed
- The sidebar's minimum width is now sized to its **title bar** rather than a flat 80px. The old floor was wider than a fully collapsed HUD, so an emptied label column made no visible difference. A HUD whose content computes narrower than 80px will now render narrower than it did before; the title can still never clip.
- The config screen's spinners are regrouped into a row of six sizing controls (Scale / Text / IGap / LPad / VPad / ECol) and a row of four (Sync / Warn / Crit / Opac). Same two rows as before — nothing moved vertically.

### Notes
- **Your existing config migrates automatically on first launch.** `colPad` seeds both `labelPad` and `valuePad`, so every HUD keeps exactly the width it had; `iconGap` takes the 3 that used to be hard-coded. 1.4.1's `hideStatNames` becomes `statNameMode` — `true` maps to `Show Abbrev`, `false` to `Show Names`. Both old keys are dropped from `nixstats.json` on the next save.
- Display only, as before — nothing about what is tracked, read, or saved changes.

### Requirements
- **Java 25**, Minecraft 26.1.x. Client-side; Fabric also needs Fabric API.

## [1.4.1] — 2026-08-15

A Names toggle that hides block/item/mob names to keep the HUD narrow.

### Added
- **Names toggle** — a `Names: On` / `Names: Off` button in the config screen hides the block/item/mob name on each HUD row, leaving only the action (*Mined*, *Used*, *Crafted*, *Broken*, *Picked Up*, *Dropped*, *Killed*, *Killed By*). The row's icon still identifies what is being counted, and the sidebar narrows to match. Stored as `hideStatNames` (default `false`) and reflected in the config screen's live preview.

### Notes
- Display only — nothing about what is tracked, read, or saved changes, and switching the toggle back restores the full names.
- Rows whose icon cannot identify them on its own always keep their full label: General stats (they share one generic icon), advancements, and the phantom timer. Those can still set the sidebar's width.

### Requirements
- **Java 25**, Minecraft 26.1.x. Client-side; Fabric also needs Fabric API.

## [1.3.1] — 2026-07-31

Advancement tracking, HUD show/hide, and opacity.

### Added
- **Advancement tracking** — a grand total across all namespaces, per-namespace totals (vanilla, a datapack, or a mod), and individual advancements with live `X/Y` criteria progress (✓ when complete). Browse them in a collapsible, searchable per-namespace tree; datapack and modded advancements are included automatically.
- **Show / Hide HUD** keybind — toggles the whole sidebar (unbound by default; persists across sessions).
- **HUD opacity** control (0%–100%) in the config screen; text and icons stay fully opaque.
- Proper display names for the keybinds in Controls.

### Fixed
- Config-screen arrows (spinners, row reorder, remove) now render crisply on every Minecraft version (drawn as pixels instead of font glyphs).

### Requirements
- **Java 25**, Minecraft 26.1.x. Client-side; Fabric also needs Fabric API.

## [1.2.4] — 2026-07-28

Housekeeping — packaging only. No gameplay changes.

### Changed
- Renamed the combined output jar from `-universal` to `-multi` for consistent naming across all versions.

### Requirements
- **Java 25**, Minecraft 26.1.x. Client-side; Fabric also needs Fabric API.

## [1.2.3] — 2026-07-01

First multi-loader release for **Minecraft 26.x** (the 26.1.x line).

### Added
- **Fabric + NeoForge** support from a single **universal** jar (per-loader `-fabric` / `-neoforge` jars are also produced).
- Minecraft **26.1, 26.1.1, and 26.1.2** compatibility.

### Changed
- **No Architectury API required** — nixStats is now fully standalone. Events are wired natively (Fabric API on Fabric, the NeoForge event bus on NeoForge).
- Version pinned to the **26.1.x** line; the jar will not load on a different minor version.
- The config screen now **scales to fit** any GUI scale (no need to lower your GUI scale).

### Removed
- The non-functional **Font (Default/Uniform)** toggle — it never changed the font. May return if/when the uniform-font API is confirmed.

### Dependencies
- **Fabric jar:** Minecraft 26.1.x, Fabric Loader >= 0.19.3, Fabric API 0.153.0+26.1.2
- **NeoForge jar:** Minecraft 26.1.x, NeoForge 26.1.2.76  *(no Fabric API, no Architectury)*
