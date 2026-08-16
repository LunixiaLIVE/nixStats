<div align="center">

# 📊 nixStats

### A configurable statistics sidebar HUD.

![](https://img.shields.io/badge/Fabric-DBA463?style=for-the-badge&logoColor=white)&nbsp;![](https://img.shields.io/badge/NeoForge-F16436?style=for-the-badge&logoColor=white)&nbsp;

[![](https://img.shields.io/badge/Download_on-Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/project/nixstats)&nbsp;[![](https://img.shields.io/badge/Download_on-CurseForge-F16436?style=for-the-badge&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/nixstats-multi)

![](https://img.shields.io/badge/Minecraft-26.x_%7C_1.21.x-62B47A?style=flat-square) ![](https://img.shields.io/badge/Side-Client--side-3498DB?style=flat-square) ![](https://img.shields.io/badge/Fabric_API-required_on_Fabric-4A90D9?style=flat-square) ![](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

</div>

---

> [!NOTE]
> **Client-side, config-driven stats HUD.** A compact sidebar that overlays a live count of whatever you
> care about — the phantom (insomnia) timer, blocks mined, mobs killed, distance walked, playtime, and
> hundreds of other vanilla stats. Everything is set up in-game from a **scale-to-fit config screen** with a
> **live preview** — no config-file editing required. Per-version code and changelog live on the
> [`multi_*`](#-versions--downloads) branches.

## ✨ Features

- **A clean sidebar HUD** — a bordered, titled panel with a per-row **item icon**, a label, and a
  right-aligned value. Rows alternate shading and columns auto-size to their content.
- **Track almost any vanilla stat.** The built-in **phantom (insomnia) timer** plus every stat Minecraft
  keeps: blocks mined, items used / crafted / broken / picked up / dropped, mobs killed, deaths *by* a mob,
  and the whole **General** family — counts, distances (walked, sprinted, flown…), and time (play time,
  time since rest…).
- **A live phantom timer with color states.** Counts down the time until phantoms can spawn and shifts
  color from **rested → warning → critical** as the clock runs out, so a glance tells you whether it's time
  to sleep.
- **Advancement tracking** *(26.x)*. A **grand total** across every namespace, **per-namespace
  totals**, or **individual advancements** with live `X/Y` criteria progress and a ✓ when complete.
  Datapack and modded advancements are picked up automatically and grouped by namespace.
- **Add stats from a visual picker.** Browse categories with a searchable **item grid** (with tooltips) or
  a scrollable list for the General stats — click an item or entry to add it. No IDs to memorize.
- **Full in-game configuration.** Title, HUD scale, text scale, column padding, sync interval, the three
  phantom colors (12-swatch palette + custom), the warning/critical thresholds, and the stat list —
  reorder, remove, add — all with a **live preview** beside the panel.
- **Name display, three ways.** One button cycles **Show Names** (the full label, *Stone Mined*),
  **Show Abbrev** (the action alone — *Mined*, *Killed*, *Picked Up*) and **Show None** (no label at all,
  leaving just the icon and the number). The button's caption always reads the mode in effect, and the
  sidebar narrows to match. Under **Show None** the same block tracked under several categories looks
  identical — that ambiguity is intended. General stats, advancements and the phantom timer keep their
  full label in every mode, since their icon alone wouldn't identify them.
- **Per-column width controls.** Tune the icon→label gap, the label column and the value column
  independently, plus the width the middle column takes when no row uses it — set that to `0` and a
  name-less HUD collapses to icons hard against the numbers. Columns still auto-fit their content.
- **Abbreviate large values** *(optional)*. Shorten big counts to `1.5K` / `2.3M` / `1.1B` so the value
  column stays narrow. Off by default, and only applied where the number shown *is* the raw count —
  distances, times, the phantom clock, advancement progress and tenths-formatted damage stats keep the
  format Minecraft gave them. Counts truncate rather than round, so a counter never reads higher than it
  actually is: 999,999 shows as `999.9K`, not `1M`.
- **Adjustable HUD opacity** *(26.x)*. Fade the background and frame from `0%`–`100%`; text and icons
  stay fully opaque so the numbers never get harder to read.
- **Show / Hide the whole HUD** *(26.x)*. A second rebindable keybind toggles the sidebar off and on,
  and the choice persists between sessions.
- **Drag-to-place positioning.** A dedicated "Set Position" mode where you drag the frame anywhere on
  screen; one click resets it to the default top-right corner.
- **Open it your way.** A rebindable keybind *(unbound by default)* **or** the `/nixstats` command.
- **Multiplayer-aware.** Periodically refreshes your stats from the server so counts stay live on servers,
  and reads straight from the integrated server in single-player for tick-accurate values.
- **Fully client-side.** Install it on your client and it works on any server — vanilla or modded. Modded
  blocks, items, mobs, and stats show up in the picker automatically because everything is read from the
  live registry.

## 🔧 How it works

nixStats draws a single sidebar panel every frame from your saved stat list. Each row resolves three
things:

- **An icon** — the item itself for item/block stats, the matching **spawn egg** for mob stats
  (skeleton-skull fallback), a **phantom membrane** for the timer, and paper for General stats.
- **A label** — a friendly, auto-generated name like *"Diamond Ore Mined"* or *"Killed by Creeper"*, which
  you can rename freely.
- **A value** — read live from your player's statistics and formatted for the stat type.

**Values are formatted to match the stat.** Every stat is rendered by Minecraft's own formatter, so counts,
distances, time and damage all read exactly as they do on the vanilla Statistics screen — counts as grouped
numbers (`1,234,567`), distances in km / m / cm, time as `d` / `h` / `m`. Advancements show their vanilla
`X/Y` criteria progress (✓ when complete). The one exception is the phantom timer, which shows a live
`MM:SS` countdown.

**The phantom timer** starts full and counts **down** toward zero — zero is when phantoms may begin
spawning after too long without sleep. Its color follows the fraction of time remaining: above the
**warning** threshold it shows the *rested* color, between warning and **critical** it shows the *warning*
color, and below critical it shows the *critical* color (green / yellow / red by default). Sleeping resets
it.

**Staying live.** In single-player, values are read directly from the integrated server every tick. On a
multiplayer server the mod periodically asks the server to resend your statistics (every few seconds, on a
configurable interval) so the numbers keep updating while you play.

## 🎛️ Configuration

Open the config screen with the **keybind** or **`/nixstats`** (see [Controls](#️-controls)).
Everything below is edited there — the panel **scales to fit** even at high GUI-scale settings, and a
**live preview** of the sidebar sits beside the controls so you see every change instantly. Nothing is
applied until you hit **Save**.

**In the config screen you can:**

- **Sidebar Title** — type any title (up to 32 characters).
- **Scale** — overall HUD size, `0.10x`–`3.00x`.
- **Text** — text size relative to the HUD, `0.50x`–`2.00x`.
- **IGap / LPad / VPad** — breathing room on each column: the icon→label gap, the label column and the
  value column, `0`–`20` each. The columns still auto-fit their content.
- **ECol** — the width the middle column takes when *no* row populates it (every row under **Show
  None**, say), `0`–`20`. At the default `0` that column disappears entirely. If even one row keeps a
  label, the column auto-fits that instead and this is ignored.
- **Sync** — how often stats refresh on servers, in seconds.
- **Phantom Colors** — pick the *Rested*, *Warning*, and *Critical* colors from a 12-swatch palette (a
  custom value from the file is shown as an extra swatch).
- **Warning / Critical** — the `%`-of-time-remaining thresholds where the phantom timer changes color.
- **Opacity** *(26.x)* — HUD background and frame opacity, `0%`–`100%` (text and icons stay opaque).
- **Names** — cycle **Show Names** → **Show Abbrev** → **Show None** for the label on each row. The
  button's caption is always the mode in effect. General stats, advancements and the phantom timer keep
  their full label in every mode, since their icon alone wouldn't identify them.
- **Values** — toggle between the full count (`1,234,567`) and an abbreviated one (`1.2M`).
- **Stats** — a scrollable list; use **↑ / ↓** to reorder, **×** to remove, and **+ Add Stat** to open the
  picker.
- **Set Position** — enter drag mode to place the frame; **Reset Position** returns it to the top-right.

### 🗂️ The stat picker

**+ Add Stat** opens a browser you page through with **`<` / `>`**:

| Category | What it lists |
|:--|:--|
| **Phantom Timer** | The built-in insomnia countdown (one click adds it). |
| **General: Counts** | Count-type custom stats (jumps, damage dealt, …). |
| **General: Distances** | Distance stats (walked, sprinted, flown, swum, …). |
| **General: Time** | Time stats (play time, time since rest, …). |
| **Items: Mined** | Breakable blocks — tracked as *blocks mined*. |
| **Items: Used** | Any item — tracked as *used*. |
| **Items: Crafted** | Craftable items (built from the server's recipes). |
| **Items: Broken** | Damageable items — tracked as *broken*. |
| **Items: Picked Up** | Any item — tracked as *picked up*. |
| **Items: Dropped** | Any item — tracked as *dropped*. |
| **Mobs: Killed** | Any mob (shown as its spawn egg) — kills. |
| **Mobs: Killed By** | Any mob — times it killed *you*. |

Item categories show a **searchable 9-wide grid** with hover tooltips; General categories show a
searchable scrolling list. Because the lists are built from the **live registry**, modded content appears
automatically.

### 📄 The `nixstats.json` file

Config is saved to **`config/nixstats.json`** and (re)written whenever you Save. You rarely need to touch
it — the in-game screen covers everything — but here are the keys and defaults:

| Key | Default | Range / notes |
|:--|:--|:--|
| `posX` | `-1` | HUD X in pixels; `-1` = auto (right edge). |
| `posY` | `-1` | HUD Y in pixels; `-1` = auto (near top). |
| `scale` | `1.0` | Overall HUD scale, `0.1`–`3.0`. |
| `textScale` | `1.0` | Text scale relative to the HUD, `0.5`–`2.0`. |
| `iconGap` | `3` | Gap between the row icon and the label, `0`–`20`. |
| `labelPad` | `2` | Padding on the label column, `0`–`20`. |
| `valuePad` | `2` | Padding on the value column, `0`–`20`. |
| `emptyLabelWidth` | `0` | Width of the label column when no row populates it, `0`–`20`. |
| `sidebarTitle` | `"nixStats"` | Title text (≤ 32 chars). |
| `colorRested` | `0xFF55FF55` | Phantom "rested" color (ARGB, green). |
| `colorWarning` | `0xFFFFFF55` | Phantom "warning" color (ARGB, yellow). |
| `colorCritical` | `0xFFFF5555` | Phantom "critical" color (ARGB, red). |
| `thresholdWarning` | `0.5` | Fraction of time left to stay *rested* (0–1). |
| `thresholdCritical` | `0.2` | Fraction of time left before *critical* (0–1). |
| `syncInterval` | `5` | Seconds between server stat refreshes. |
| `hudOpacity` | `1.0` | Background/frame opacity, `0.1`–`1.0` *(26.x)*. |
| `hudHidden` | `false` | Whether the HUD is toggled off *(26.x)*. |
| `statNameMode` | `"NAMES"` | Label display: `NAMES`, `ABBREV` (action only) or `NONE`. |
| `abbreviateValues` | `false` | Shorten large plain counts to `1.5K` / `2.3M` / `1.1B`. |
| `stats` | *(Phantom)* | Ordered list of tracked stats; each has `statType`, `targetId`, and `label`. |

> [!NOTE]
> **Updating from 1.4.1 or earlier?** The file migrates itself the first time 1.4.3 loads. The old
> `colPad` seeds both `labelPad` and `valuePad`, so your HUD keeps exactly the width it had, and
> `hideStatNames` becomes `statNameMode` (`true` → `ABBREV`, `false` → `NAMES`). Both old keys are
> dropped from the file on the next save.

> [!TIP]
> A `stats` entry is just `{ "statType": "...", "targetId": "...", "label": "..." }`. `statType` is one of
> `phantom`, `block_mined`, `item_used`, `item_crafted`, `item_broken`, `item_picked_up`, `item_dropped`,
> `entity_killed`, `entity_killed_by`, `custom`, `advancement`, or `advancement_total`; `targetId` is the registry ID it points at (`null` for
> the phantom timer). The picker fills all of this in for you.

## ⌨️ Controls

| Action | How |
|:--|:--|
| **Open the config screen** | The **nixStats config** keybind *(unbound by default — set it under Controls → nixStats)* or the **`/nixstats`** command. |
| **Show / hide the HUD** | The **Show / Hide nixStats HUD** keybind *(unbound by default)*. |
| **Reorder / remove a stat** | The **↑ / ↓ / ×** buttons on each row in the config screen. |
| **Move the HUD** | **Set Position** → drag the frame; **Reset Position** to restore the default corner. |

## 💡 Use cases

- **Never get ambushed by phantoms.** Keep the insomnia timer on screen and sleep when it turns yellow.
- **Grind tracking.** Watch *Ancient Debris Mined*, *Mob X Killed*, or *Item Crafted* tick up during a
  farming or mining session.
- **Personal challenges & runs.** Surface *Deaths*, *Damage Taken*, *Play Time*, or *Distance by Elytra*
  for a self-imposed challenge or a speed-goal.
- **Streaming & recording overlays.** A tidy, scalable, repositionable panel of exactly the numbers you
  want on camera — no external overlay tool needed.
- **Modpack dashboards.** Because modded stats register automatically, you can pin mod-specific counters
  right alongside vanilla ones.

## 📦 Versions &amp; downloads

> [!NOTE]
> This repo uses a **branch-per-version** layout. This `main` branch is **documentation only** — the code for each Minecraft version lives on its own branch, each with an independent history and its own `CHANGELOG.md`.

| Branch | Minecraft | Loaders | Dependencies | Log |
|:------:|:---------:|:-------:|:------------:|:---:|
| [`multi_26.2`](https://github.com/LunixiaLIVE/nixStats/tree/multi_26.2) | 26.2.x | Fabric · NeoForge | Fabric API *(Fabric only)* | [📄](https://github.com/LunixiaLIVE/nixStats/blob/multi_26.2/CHANGELOG.md) |
| [`multi_26.1`](https://github.com/LunixiaLIVE/nixStats/tree/multi_26.1) | 26.1, 26.1.1, 26.1.2 | Fabric · NeoForge | Fabric API *(Fabric only)* | [📄](https://github.com/LunixiaLIVE/nixStats/blob/multi_26.1/CHANGELOG.md) |
| [`multi_1.21.11`](https://github.com/LunixiaLIVE/nixStats/tree/multi_1.21.11) | 1.21.11 | Fabric · NeoForge | Fabric API *(Fabric only)* | [📄](https://github.com/LunixiaLIVE/nixStats/blob/multi_1.21.11/CHANGELOG.md) |
| [`multi_1.21.9`](https://github.com/LunixiaLIVE/nixStats/tree/multi_1.21.9) | 1.21.9–1.21.10 | Fabric · NeoForge | Fabric API *(Fabric only)* | [📄](https://github.com/LunixiaLIVE/nixStats/blob/multi_1.21.9/CHANGELOG.md) |

> [!TIP]
> Every `multi_*` branch builds **one jar that runs on both Fabric and NeoForge**. On 26.x that's a shared universal jar (Minecraft is unobfuscated there); on 1.21.x it's a jar-in-jar bundle (`-multi.jar`) with the Fabric and NeoForge builds nested inside, each loader picking its own. Per-loader `-fabric` / `-neoforge` jars are produced too (`build/staging/`). Fully self-contained — **no extra library mods to install**.

<details>
<summary>🛠️ <b>Building from source</b></summary>

Each code branch is a self-contained Gradle project. Grab the branch for your Minecraft version:

```bash
git clone -b multi_26.2 https://github.com/LunixiaLIVE/nixStats.git
cd nixStats
./gradlew build
```

The build output lands in `build/libs/` (a universal jar on 26.x, a `-multi.jar` jar-in-jar bundle on 1.21.x) — drop it into your `mods/` folder on either loader.
</details>

## 📄 License

Released under the **MIT License**.

<div align="center"><sub>⛏️ Part of <a href="https://github.com/LunixiaLIVE/Lunixia-Minecraft-QOL-Mods">Lunixia's Minecraft QOL Mods</a>.</sub></div>
