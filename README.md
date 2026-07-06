<div align="center">

# 📊 nixStats

### A configurable statistics sidebar HUD.

![](https://img.shields.io/badge/Fabric-DBA463?style=for-the-badge&logoColor=white)&nbsp;![](https://img.shields.io/badge/NeoForge-F16436?style=for-the-badge&logoColor=white)&nbsp;

[![](https://img.shields.io/badge/Download_on-Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/project/nixstats)&nbsp;[![](https://img.shields.io/badge/Download_on-CurseForge-F16436?style=for-the-badge&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/nixstats)

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
  right-aligned value. Rows alternate shading, columns auto-size to their content, and big numbers collapse
  to `K` / `M` / `B` so the frame stays tidy.
- **Track almost any vanilla stat.** The built-in **phantom (insomnia) timer** plus every stat Minecraft
  keeps: blocks mined, items used / crafted / broken / picked up / dropped, mobs killed, deaths *by* a mob,
  and the whole **General** family — counts, distances (walked, sprinted, flown…), and time (play time,
  time since rest…).
- **A live phantom timer with color states.** Counts down the time until phantoms can spawn and shifts
  color from **rested → warning → critical** as the clock runs out, so a glance tells you whether it's time
  to sleep.
- **Add stats from a visual picker.** Browse categories with a searchable **item grid** (with tooltips) or
  a scrollable list for the General stats — click an item or entry to add it. No IDs to memorize.
- **Full in-game configuration.** Title, HUD scale, text scale, column padding, sync interval, the three
  phantom colors (12-swatch palette + custom), the warning/critical thresholds, and the stat list —
  reorder, remove, add — all with a **live preview** beside the panel.
- **Drag-to-place positioning.** A dedicated "Set Position" mode where you drag the frame anywhere on
  screen; one click resets it to the default top-right corner.
- **Open it your way.** A rebindable keybind *(unbound by default)* **or** the `/nixstats config` command.
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

**Values are formatted to match the stat.** The phantom timer and playtime-style stats render as a clock
(`MM:SS` for the timer, `H:MM:SS` for time stats); distances and counts render as plain numbers, with
`1.5K` / `2.3M` / `1.1B` shortening once they get large.

**The phantom timer** starts full and counts **down** toward zero — zero is when phantoms may begin
spawning after too long without sleep. Its color follows the fraction of time remaining: above the
**warning** threshold it shows the *rested* color, between warning and **critical** it shows the *warning*
color, and below critical it shows the *critical* color (green / yellow / red by default). Sleeping resets
it.

**Staying live.** In single-player, values are read directly from the integrated server every tick. On a
multiplayer server the mod periodically asks the server to resend your statistics (every few seconds, on a
configurable interval) so the numbers keep updating while you play.

## 🎛️ Configuration

Open the config screen with the **keybind** or **`/nixstats config`** (see [Controls](#️-controls)).
Everything below is edited there — the panel **scales to fit** even at high GUI-scale settings, and a
**live preview** of the sidebar sits beside the controls so you see every change instantly. Nothing is
applied until you hit **Save**.

**In the config screen you can:**

- **Sidebar Title** — type any title (up to 32 characters).
- **Scale** — overall HUD size, `0.10x`–`3.00x`.
- **Text** — text size relative to the HUD, `0.50x`–`2.00x`.
- **Col Pad** — extra padding on each column, `0`–`20`.
- **Sync** — how often stats refresh on servers, in seconds.
- **Phantom Colors** — pick the *Rested*, *Warning*, and *Critical* colors from a 12-swatch palette (a
  custom value from the file is shown as an extra swatch).
- **Warning / Critical** — the `%`-of-time-remaining thresholds where the phantom timer changes color.
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
| `colPad` | `2` | Extra per-column padding, `0`–`20`. |
| `sidebarTitle` | `"nixStats"` | Title text (≤ 32 chars). |
| `colorRested` | `0xFF55FF55` | Phantom "rested" color (ARGB, green). |
| `colorWarning` | `0xFFFFFF55` | Phantom "warning" color (ARGB, yellow). |
| `colorCritical` | `0xFFFF5555` | Phantom "critical" color (ARGB, red). |
| `thresholdWarning` | `0.5` | Fraction of time left to stay *rested* (0–1). |
| `thresholdCritical` | `0.2` | Fraction of time left before *critical* (0–1). |
| `syncInterval` | `5` | Seconds between server stat refreshes. |
| `stats` | *(Phantom)* | Ordered list of tracked stats; each has `statType`, `targetId`, and `label`. |

> [!TIP]
> A `stats` entry is just `{ "statType": "...", "targetId": "...", "label": "..." }`. `statType` is one of
> `phantom`, `block_mined`, `item_used`, `item_crafted`, `item_broken`, `item_picked_up`, `item_dropped`,
> `entity_killed`, `entity_killed_by`, or `custom`; `targetId` is the registry ID it points at (`null` for
> the phantom timer). The picker fills all of this in for you.

## ⌨️ Controls

| Action | How |
|:--|:--|
| **Open the config screen** | The **nixStats config** keybind *(unbound by default — set it under Controls → nixStats)* or the **`/nixstats config`** command. |
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

The universal jar lands in `build/libs/` — drop it into your `mods/` folder on either loader.
</details>

## 📄 License

Released under the **MIT License**.

<div align="center"><sub>⛏️ Part of <a href="https://github.com/LunixiaLIVE/Lunixia-Minecraft-QOL-Mods">Lunixia's Minecraft QOL Mods</a>.</sub></div>
