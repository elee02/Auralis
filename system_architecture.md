# System Architecture for Auralis

## 1. Architecture Overview
A modular, event-driven design separates audio, data, lyrics, and UI layers—maximizing performance, offline capability, and extensibility without paid services.

```
┌──────────────────┐     ┌──────────────────┐     ┌────────────────────┐
│   UI Layer       │◀──▶│  Player Layer    │◀──▶│  Data & Lyrics     │
│ (Compose Views,  │     │ (ExoPlayer core) │     │ Layer (Room DB,    │
│  Theme Engine)   │     │                  │     │  LrcParser)        │
└──────────────────┘     └──────────────────┘     └────────────────────┘
        ▲                         ▲                          ▲
        │                         │                          │
        ▼                         ▼                          ▼
   Settings     ─────────▶   MediaSession   ─────▶   On-disk LRC, edits
```

## 2. Core Modules

### 2.1 UI Layer
- Jetpack Compose screens & widgets
- Theme & layout engine driven by user prefs
- SynchronizedLyricsView component for live highlight & scroll
- WaveformSeekBar: renders pre-computed audio waveform amplitudes; handles touch-to-seek
- WaveformSeekBar (for music)
- SkipButtonsComponent: renders “–X s/+X s” buttons, wired to player.seekTo(current –/+ X * 1000)


### 2.2 Player Layer
- **ExoPlayer** core with:
  - Gapless/ crossfade extension
  - Playback speed control (audio processor)
  - Equalizer & audio effects via Android AudioFX
- **MediaSessionService**
  - Handles lock-screen & widget events
  - Manages notification through PlayerNotificationManager

### 2.3 Data & Lyrics Layer
- **Room Database**  
  - Entities: Song, Playlist, LyricsEdit  
  - DAOs for fast queries and incremental updates
- **Lyrics Engine**  
  - **Parser**: LrcParser for `[mm:ss.xx]→text`
  - **Extractor**: embedded lyrics via MediaMetadataRetriever
  - **Cache**: local edits stored in DB; no network dependency

## 3. Data Flow

1. **Startup**
   - Read user preferences (theme, offsets)
   - Initialize MediaSession & ExoPlayer
   - Load last-played track & lyrics

2. **Library Scan**
   - MediaScanner queries MediaStore → builds/updates Room tables
   - Emits events for UI to refresh

3. **Playback**
   - User selects a track → PlayerLayer prepares media item
   - Lyrics Engine retrieves parsed lines + global offset
   - UI subscribes to playback position → highlights appropriate lyric line

4. **User Actions**
   - Seek / skip → PlayerLayer updates position
   - Adjust offset / edit lyric → Lyrics DB update → UI reloads from DB

## 4. Performance & Efficiency

- **Lazy Loading**: Only parse lyrics when user opens lyrics pane
- **Batch Scans**: Incremental media updates; avoid full rescans
- **Background Threads**: Off-UI threading for DB and parsing
- **Battery Saver**: Throttle updates to 10 Hz when low battery

## 5. Extensibility

- **Plugin API** (future): support add-ons for visualizers or theme packs
- **Config-Driven**: JSON schema for layout presets and color palettes
- **Test Coverage**: Unit tests for parser, DB migrations, and UI state
