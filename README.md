<div align="center">

# 📊 nixStats

### A configurable statistics sidebar HUD.

![](https://img.shields.io/badge/Fabric-DBA463?style=for-the-badge&logoColor=white)&nbsp;![](https://img.shields.io/badge/NeoForge-F16436?style=for-the-badge&logoColor=white)&nbsp;

[![](https://img.shields.io/badge/Download_on-Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/project/nixstats)&nbsp;[![](https://img.shields.io/badge/Download_on-CurseForge-F16436?style=for-the-badge&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/nixstats)

![](https://img.shields.io/badge/Minecraft-26.x_%7C_1.21.x-62B47A?style=flat-square) ![](https://img.shields.io/badge/Side-Client--side-3498DB?style=flat-square) ![](https://img.shields.io/badge/Fabric_API-required_on_Fabric-4A90D9?style=flat-square) ![](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

</div>

---

## ✨ Features

- Sidebar HUD: phantom timer, blocks/items/entities, any vanilla stat
- Config screen (scale-to-fit on 26.x) with live preview
- Keybind + /nixstats config to open settings

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
