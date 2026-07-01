# nixStats — Changelog

A configurable statistics sidebar HUD.
Client-side only.

Format based on [Keep a Changelog](https://keepachangelog.com/); versioning per [SemVer](https://semver.org/).

## [1.2.3] — 2026-07-01

First multi-loader release for **Minecraft 26.x** (the 26.2.x line).

### Added
- **Fabric + NeoForge** support from a single **universal** jar (per-loader `-fabric` / `-neoforge` jars are also produced).
- Minecraft **26.2** compatibility.

### Changed
- **No Architectury API required** — nixStats is now fully standalone. Events are wired natively (Fabric API on Fabric, the NeoForge event bus on NeoForge).
- Version pinned to the **26.2.x** line; the jar will not load on a different minor version.
- The config screen now **scales to fit** any GUI scale (no need to lower your GUI scale).

### Removed
- The non-functional **Font (Default/Uniform)** toggle — it never changed the font. May return if/when the uniform-font API is confirmed.

### Dependencies
- **Fabric jar:** Minecraft 26.2.x, Fabric Loader >= 0.19.3, Fabric API 0.153.0+26.2
- **NeoForge jar:** Minecraft 26.2.x, NeoForge 26.2.0.7-beta  *(no Fabric API, no Architectury)*
