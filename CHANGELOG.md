# nixStats — Changelog

A configurable statistics sidebar HUD.
Client-side only.

Format based on [Keep a Changelog](https://keepachangelog.com/); versioning per [SemVer](https://semver.org/).

## [1.2.3] — 2026-07-02

Multi-loader release for **Minecraft 1.21.9–1.21.10** (a single jar covering both).

### Added
- **Fabric + NeoForge** support from a single **universal** jar (per-loader `-fabric` / `-neoforge` jars are also produced).
- Minecraft **1.21.9 and 1.21.10** compatibility (the jar stops at 1.21.10 — 1.21.11 renamed `Identifier` and lives on `multi_1.21.11`; 1.21.8 and below changed the input/keybind API).

### Changed
- **No Architectury API required** — nixStats is now fully standalone. Events are wired natively (Fabric API on Fabric, the NeoForge event bus on NeoForge).

### Dependencies
- **Fabric jar:** Minecraft 1.21.9–1.21.10, Fabric Loader >= 0.19.2, Fabric API 0.134.1+1.21.9
- **NeoForge jar:** Minecraft 1.21.9–1.21.10, NeoForge 21.9.x–21.10.x  *(no Fabric API, no Architectury)*
