# nixStats

A configurable statistics sidebar HUD.
**Client-side only.**

## Features

- Sidebar HUD: phantom timer, blocks/items/entities, any vanilla stat
- Config screen (scale-to-fit on 26.x) with live preview
- Keybind + /nixstats config to open settings

## Versions & downloads

This repository uses a **branch-per-version** layout: this `main` branch is documentation only — the code for each Minecraft version lives on its own branch, each with its own history and `CHANGELOG.md`.

| Branch | Minecraft | Loaders | Dependencies | Notes |
|--------|-----------|---------|--------------|-------|
| [`multi_26.2`](https://github.com/LunixiaLIVE/nixStats/tree/multi_26.2) | 26.2.x | Fabric · NeoForge | Fabric API *(Fabric only)* | [changelog](https://github.com/LunixiaLIVE/nixStats/blob/multi_26.2/CHANGELOG.md) |
| [`multi_26.1`](https://github.com/LunixiaLIVE/nixStats/tree/multi_26.1) | 26.1, 26.1.1, 26.1.2 | Fabric · NeoForge | Fabric API *(Fabric only)* | [changelog](https://github.com/LunixiaLIVE/nixStats/blob/multi_26.1/CHANGELOG.md) |
| [`multi_1.21.11`](https://github.com/LunixiaLIVE/nixStats/tree/multi_1.21.11) | 1.21.11 | Fabric · NeoForge | Fabric API *(Fabric only)* | [changelog](https://github.com/LunixiaLIVE/nixStats/blob/multi_1.21.11/CHANGELOG.md) |
| [`multi_1.21.9`](https://github.com/LunixiaLIVE/nixStats/tree/multi_1.21.9) | 1.21.9–1.21.10 | Fabric · NeoForge | Fabric API *(Fabric only)* | [changelog](https://github.com/LunixiaLIVE/nixStats/blob/multi_1.21.9/CHANGELOG.md) |

The `multi_*` branches each build a single **universal** jar that runs on **both** Fabric and NeoForge (per-loader `-fabric` / `-neoforge` jars are also produced), and are fully standalone — **no Architectury API at runtime**.

## License

MIT
