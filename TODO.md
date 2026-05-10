# TPlayer TODO List

## UI Fixes & Improvements
- [ ] **Stats Screen Visuals**: The `StatsScreen` currently lists most played songs but lacks actual statistics. Implement the TODO `// Optional: Add play count and duration badge` so users can see exact play counts and duration.
- [ ] **Player Screen Controls Overflow**: The bottom control row in `PlayerScreen` contains 7 icons (Shuffle, Lyrics, A-B, Speed, Timer, Playlist, Repeat). This is too crowded and will likely overflow on smaller devices. Group secondary actions into a "More" bottom sheet or overflow menu.
- [ ] **Mini Player Gestures**: The `MiniPlayer` is static. Add swipe gestures (e.g., swipe left/right to skip tracks, swipe down to dismiss/stop).
- [ ] **Search Input Debounce**: In `SearchScreen`, the `searchSongs` function is called on every keystroke. Add a debounce mechanism (e.g., 300ms) to avoid performance lag.
- [ ] **Lyrics Formatting**: The `LyricsScreen` currently displays text via simple vertical scroll. Upgrade this to support synchronized lyrics (LRC) with active line highlighting and auto-scroll.
- [ ] **Missing Equalizer UI**: There is no button to access the Equalizer. Add an Equalizer icon to the `PlayerScreen` top app bar or inside a "More Options" menu.

## Advanced Features & Missing Functionalities
- [ ] **Playlist Song Management**: `PlaylistDetailScreen` lacks the ability to reorder songs via drag-and-drop.
- [ ] **Add to Playlist**: Add an explicit action in `PlayerScreen` and `LibraryGroupDetailScreen` to add a specific song to an existing or new playlist.
- [ ] **System Audio Effects (Equalizer)**: Implement the actual Equalizer view and connect it to `PlaybackService` audio sessions.
- [ ] **Crossfade / Gapless Settings Verification**: Ensure the settings configured in `SettingsScreen` (Crossfade duration, Gapless playback) seamlessly update the ExoPlayer configuration in real-time.
- [ ] **Shared Element Transitions**: Implement shared element transitions when expanding the `MiniPlayer` into the full `PlayerScreen` for a premium feel.


Now remove the padding on the top screen. In between the Top App Bar and the status bar, there is a paddin in all the pages, remove the padding.
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/CurrentPlaylistScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/HomeScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/LibraryGroupDetailScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/LyricsScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/PlayerScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/PlaylistDetailScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/PlaylistScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/SearchScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/SettingsScreen.kt
/home/oluwatosin/Documents/code/opensource/TPlayer/app/src/main/java/com/tosin/musicplayer/ui/screens/StatsScreen.kt
