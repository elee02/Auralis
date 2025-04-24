# Auralis: Android Music Player with Lyrics Synchronization

## 1. Overview
Auralis is a high-fidelity, offline-first Android music player that brings Poweramp-level audio quality and seamless, customizable lyrics alignment—all without paid third-party APIs. Enjoy local playback, manual and automatic lyric sync, and a fully themeable UI for a premium yet efficient experience.

## 2. Key Features

- **Audio Playback**
  - Formats: MP3, FLAC, AAC, WAV, OGG, and more
  - Gapless playback, crossfade, replay gain support
  - Built-in 10-band equalizer with bass boost and virtualizer
  - Variable playback speed (0.5×–2.0×) with pitch correction

- **Lyrics Synchronization**
  - **Automatic**: Parses local LRC/txt files and embedded lyrics
  - **Manual Adjustment**: Long-press any line to nudge timing; “Calibrate” mode for global offset
  - **On-the-fly Editing**: Edit text or timestamps in-app
  - **Fallback**: If no local lyrics, prompts user to paste or import LRC; no network required

- **Customizable Interface**
  - Theme engine: Light, Dark, AMOLED or user-defined color palettes
  - Font selector and size slider for lyrics and UI
  - Layout presets: Compact (focus on album art), Expanded (lyrics + controls side by side), Mini-player mode
  - Widget and lock-screen art with adjustable opacity

- **Library Management**
  - Fast local scan with incremental updates
  - Metadata editor: Artist, album, artwork override
  - Smart playlists (recent, favorites, top-played)
  - Cross-device export/import of library DB

- **Utilities**
  - Sleep timer, alarm wake-up playlist
  - Lock-screen controls & in-notification controls
  - Headset & Bluetooth action mapping
  - Battery-aware background mode

## 3. System Requirements

- Android 8.0 (Oreo) or above
- ~50 MB free storage (plus your music)
- No mandatory Internet; optional for optional artwork lookup (free image search)

## 4. Installation

1. Download from Google Play or our GitHub Releases.
2. Grant media-read permissions on first launch.
3. (Optional) Enable “Artwork Lookup” to fetch covers from free public domain sources.

## 5. Usage Guide

### 5.1. Browsing & Playback
1. **Library**: Tap menu → Library; use filters or search.
2. **Play**: Tap a track to start.
3. **Controls**: Swipe up on mini-player to reveal full controls (seek bar, EQ, speed).

### 5.2. Lyrics
1. Swipe lyrics pane up/down to show/hide.
2. **Sync**:  
   - **Auto**: App reads local LRC or embedded lyrics.  
   - **Manual**: Long-press a line → “Adjust time” → fine‐tune offset.
3. **Edit**: Tap “✎” to edit text or timestamps.
4. **Import**: From lyrics pane menu → “Import LRC file” or paste raw lyrics.

### 5.3. Customization
1. Menu → Settings → Appearance:
   - Choose theme, accent colors, font family & size.
2. Menu → Settings → Player Layout:
   - Select Compact/Expanded/Mini modes.
3. Widgets: Home-screen → long-press → Widgets → Auralis

## 6. Technical Components

- **Audio Engine**: ExoPlayer with custom gapless & crossfade modules
- **Data Layer**: Room database stores media metadata, playback history, user edits
- **Lyrics Engine**: 
  - LrcParser: parses `[mm:ss.xx] line` into timestamped list
  - SynchronizedLyricsView: highlights and scrolls current line
- **UI Layer**: Jetpack Compose for dynamic theming and lightweight rendering
- **Background Services**: MediaSessionService for lock-screen, notification, and widget controls

```kotlin
// Example: Alpine-style manual offset calibration
fun adjustGlobalOffset(deltaMs: Long) {
    lyricsOffsetMs = (lyricsOffsetMs + deltaMs).coerceIn(-10_000, 10_000)
}
```

## 7. Troubleshooting & Tips

- **No lyrics?** Use manual import or paste.
- **Laggy UI?** Disable “Realtime visualizer” in settings.
- **Battery drain?** Enable “Battery saver mode” to pause visualizer and lower thread priority.