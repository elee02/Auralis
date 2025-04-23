# Android Music Player with Lyrics Synchronization

## Overview
The Android Music Player is an advanced audio playback application with synchronized lyrics support, offering a Poweramp-like experience with additional focus on lyrics integration. The app supports local music playback with real-time lyrics display.

## Key Features

- **High-Quality Audio Playback**: Supports MP3, FLAC, AAC, WAV, and more
- **Lyrics Synchronization**: Displays lyrics in perfect sync with music
- **Multiple Lyrics Sources**: Embedded, local LRC files, and online databases
- **Customizable Interface**: Themes, colors, and layout options
- **Advanced Audio Controls**: Equalizer, bass boost, and playback speed

## System Requirements

- Android 8.0 (Oreo) or higher
- 50MB free storage space
- Internet connection for lyrics fetching (optional)

## Installation

1. Download the APK from our website or Google Play Store
2. Enable "Install from unknown sources" if needed
3. Run the installer and follow prompts
4. Grant necessary permissions when first launched

## Usage Guide

### Basic Playback
1. Open the app and browse your music library
2. Select a song to begin playback
3. Use on-screen controls to manage playback

### Lyrics Features
1. During playback, swipe up to view lyrics
2. Tap on lyrics to seek to that position
3. Long-press lyrics to adjust synchronization

### Library Management
1. Swipe from left to access library views
2. Create playlists by selecting "New Playlist"
3. Use search function to find specific tracks

### Settings
1. Access settings from the navigation drawer
2. Adjust audio, display, and lyrics preferences
3. Enable dark mode for nighttime use

## Technical Components

### Audio Playback
```kotlin
val exoPlayer = ExoPlayer.Builder(context).build()
exoPlayer.setMediaItem(MediaItem.fromUri("content://media/audio/123"))
exoPlayer.prepare()
exoPlayer.play()