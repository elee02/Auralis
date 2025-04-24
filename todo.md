# Auralis Development Roadmap

## Phase 0: MVP (v0.1)
### Setup & Core
- [x] Project scaffold: modules, Gradle, manifest
- [x] ExoPlayer integration for basic playback
- [~] MediaScanner → Room DB for local library (basic implementation)
- [~] Playback controls: play/pause implemented, next/prev/seek unclear
- [ ] Skip backward/forward buttons (configurable 5 s, 10 s, 30 s intervals) 
- [x] Simple now-playing UI (album art + controls)

### Lyrics Sync
- [x] LRC parser & embedded lyric extractor
- [x] SynchronizedLyricsView: scrolling + highlight
- [ ] Global offset adjustment UI
- [ ] Manual lyric import & editing

### Basic Utilities
- [x] Sleep timer
- [x] Notification & lock-screen controls
- [ ] Unit tests for parser & DB

## Phase 1: Polishing & Customization (v1.0)
### Audio Enhancements
- [ ] 10-band equalizer + presets
- [ ] Crossfade, gapless playback
- [ ] Variable speed + pitch correction

### UI & Theming
- [ ] Jetpack Compose migration for all screens
- [ ] Theme engine: Light/Dark/Amoled + custom accents
- [ ] Font family & size selectors
- [ ] Layout presets: Compact, Expanded, Mini
- [ ] Waveform visualizer & scrubber on the playback bar

### Library & Playlists
- [ ] Metadata editor (tags, artwork override)
- [ ] Smart playlists: recent, favorites, top played
- [ ] Export/import library DB

### Performance & Testing
- [ ] Incremental media scan optimization
- [ ] Battery-aware “low-power mode”
- [ ] Instrumentation tests for UI & lifecycle
- [ ] Performance profiling (CPU, memory, battery)

## Phase 2: Advanced & Extensibility (v2.0+)
### Premium-Tier Features
- [ ] Plugin API for visualizers & themes
- [ ] Automatic artwork lookup (free public domain)
- [ ] Live lyric search (offline word-match)
- [ ] Widget configuration presets

### Integrations (Optional/Efficiency-Driven)
- [ ] Wear OS companion (defer if resource-heavy)
- [ ] Android Auto support (deferred if impacts APK size)
- [ ] Backup/Restore settings (local file only)

### Release Prep
- [ ] App icon, promo graphics & store listing
- [ ] Complete user documentation
- [ ] Beta testing & feedback collection
- [ ] Final Play Store compliance checks