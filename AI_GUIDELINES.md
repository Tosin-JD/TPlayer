**Material 3 Expressive + MVVM Setup for Beautiful Android App**

- **Project Structure (MVVM)**:  
  - `data/` (Repository, DataSource, API/DB)  
  - `domain/` (UseCases, Models, Repository interfaces)  
  - `ui/` (ViewModels, Screens/Composables, Theme, Navigation)  
  - `di/` (Hilt/Dagger modules)  

- **Dependencies (latest stable/expressive)**:  
  - Material3 Compose with Expressive support (`androidx.compose.material3:material3` + BOM)  
  - `androidx.compose.material3:material3-window-size-class`  
  - ViewModel + LiveData/Flow, Hilt, Navigation Compose, Coil for images  
  - Enable `android.useAndroidX=true`, `compose` compiler, latest AGP/Kotlin  

- **Theme Setup (Theme.kt)**:  
  - Use `MaterialExpressiveTheme` or `MaterialTheme` with `MotionScheme.expressive()`  
  - Dynamic colors: `dynamicLightColorScheme`/`dynamicDarkColorScheme` (Android 12+) with seed color fallback  
  - Custom `ColorScheme`, `Typography` (variable fonts if possible), `Shapes` (rounded/expressive library with variety: circles, rounded rects, custom morphing)  
  - `shapes = Shapes(...)` with expressive corner families (e.g., rounded, cut)  

- **Expressive Design Elements**:  
  - **Colors**: Dynamic/Material You + expressive palettes (vibrant accents, wide accessible range)  
  - **Shapes**: Variety — rounded containers, morphing shapes, non-rectangular emphasis elements  
  - **Typography**: Scale with expressive weights/sizes, variable fonts for fluidity  
  - **Elevation & Containment**: Layered surfaces with shadows, depth via expressive tactics  

- **Icons**:  
  - Exclusively **Material Symbols Rounded** (or custom rounded SVGs)  
  - Use `Icon` with `material-icons-rounded` font or vector assets  
  - Themed icons where possible (tint with colorScheme)  

- **Animations & Motion (Fluid Expressive)**:  
  - `MotionScheme.expressive()` at theme level (springs, physics-based)  
  - `AnimatedVisibility`, `AnimatedContent`, `animate*AsState`, shared element transitions, morphing containers  
  - Use motion tokens for consistent duration/easing (emphasize hero moments, key interactions)  
  - Spring animations for buttons, cards, navigation, toggles  

- **Components & UI Polish**:  
  - All Material 3 components (elevated/filled/tonal/outlined variants)  
  - Adaptive layouts with WindowSizeClass  
  - Responsive, accessible (min touch targets, contrast)  
  - Edge-to-edge, predictive back, splash screen API  
  - Rounded corner emphasis everywhere, subtle animations on focus/scroll/state changes  

- **Build & Compile Fixes (for ./gradlew assembleDebug)**:  
  - Sync Gradle, clean/rebuild, invalidate caches  
  - Use correct BOM: `implementation(platform(libs.androidx.compose.bom))`  
  - Enable Compose compiler reports if needed, check Kotlin/Compose version compatibility  
  - Add missing permissions/repositories, resolve dependency conflicts (Material3 + Expressive alpha/beta if used)  
  - Ensure `minSdk` supports dynamic color (21+ but best 12+ features)  
  - Fix any unresolved references in Theme or MotionScheme imports  

- **Extra Beauty Touches**:  
  - Personalized dynamic theming from wallpaper  
  - Expressive tactics: attention via size/shape/motion/color variety (sparingly)  
  - Consistent semantic tokens, accessibility defaults  
  - Test fluid animations on different devices/sizes  

**Project Overview**

This repository is an Android music player built with Jetpack Compose and an MVVM-style organization. The app is opinionated around Material 3 expressive theming, a simple ViewModel-backed UI, and a media playback controller using Media3.

**Recommended Quick Paths**
- App entry: [app/src/main/java/com/tosin/musicplayer/MainActivity.kt](app/src/main/java/com/tosin/musicplayer/MainActivity.kt)
- Home screen: [app/src/main/java/com/tosin/musicplayer/ui/screens/HomeScreen.kt](app/src/main/java/com/tosin/musicplayer/ui/screens/HomeScreen.kt)
- ViewModel: [app/src/main/java/com/tosin/musicplayer/ui/viewmodel/PlayerViewModel.kt](app/src/main/java/com/tosin/musicplayer/ui/viewmodel/PlayerViewModel.kt)
- Player controller: [app/src/main/java/com/tosin/musicplayer/player/PlayerController.kt](app/src/main/java/com/tosin/musicplayer/player/PlayerController.kt)
- Local music loader: [app/src/main/java/com/tosin/musicplayer/data/local/MusicLoader.kt.kt](app/src/main/java/com/tosin/musicplayer/data/local/MusicLoader.kt.kt)

**Project Structure (practical)**
- `app/src/main/java/com/tosin/musicplayer/` — application package and entrypoints (Activity, PlayerController, data, ui)
- `app/src/main/java/com/tosin/musicplayer/ui/screens/` — Compose screens (e.g., `HomeScreen`) and UI layer
- `app/src/main/java/com/tosin/musicplayer/ui/viewmodel/` — ViewModels exposing `StateFlow` UI state
- `app/src/main/java/com/tosin/musicplayer/data/` — repositories and local loaders (`MusicLoader` reads from MediaStore)
- `app/src/main/res/` — resources: layouts, navigation, drawables, themes

**Important Implementation Notes (must-know for AI agents)**
- ViewModel construction: `PlayerViewModel` requires a `MusicRepository` and `PlayerController`. `MusicRepository` requires a `MusicLoader` (needs a `ContentResolver`). `PlayerController` requires an Android `Context`.
  - Typical pattern used in `MainActivity` is a small `ViewModelProvider.Factory` that constructs these classes with `contentResolver` and the activity `Context`.
- `MusicLoader.loadSongs()` queries `MediaStore` on `Dispatchers.IO` and returns a list of songs. Tests or CI should mock `MusicLoader` or `MusicRepository` to avoid MediaStore dependency.
- `HomeScreen` expects a `PlayerViewModel` and an `onNavigateToPlayer: () -> Unit` callback. The `HomeScreen` displays songs via `LazyColumn` and calls `viewModel.onSongClick(...)` when items are clicked.
- Media playback: `PlayerController` uses Media3 `MediaController` built asynchronously via a `SessionToken`. It exposes `currentSong`, `isPlaying`, and `progress` as `StateFlow`s.

**Conventions & Patterns**
- UI state flows: Prefer `StateFlow` in `ViewModel` and `collectAsState()` in composables.
- No DI framework required to run locally—manual factories are used. If adding DI (Hilt), provide a module for `PlayerController` (Context), `MusicLoader` (ContentResolver), and `MusicRepository`.
- Keep long-running IO off main thread (already handled): `MusicLoader` uses `withContext(Dispatchers.IO)`.

**Build & Run**
- Build locally with Gradle wrapper from repo root:

```bash
./gradlew assembleDebug
```

- Expected successful build output: `BUILD SUCCESSFUL`.
- If running on CI or headless environments, mock or stub `MusicLoader`/MediaStore access and `PlayerController` behavior.

**Debugging Tips**
- If `MediaStore` queries return empty or crash, check runtime permissions (`READ_EXTERNAL_STORAGE` on older Android versions) and emulator media availability.
- To reproduce fast locally, replace `MusicLoader` with a fake provider that returns a small static list of `Song` objects.

**Testing & Automation**
- Unit tests should avoid Android framework dependencies. Extract repository interfaces and provide Kotlin/JVM-friendly fakes.
- For instrumentation tests that interact with MediaStore or Media3, use emulators with preloaded media or grant required permissions in the test setup.

**What to change when extending the app**
- Adding navigation: prefer `Navigation Compose` and a single `NavHost` in `MainActivity`.
- If you add DI (Hilt), convert the manual `ViewModelProvider.Factory` into injectable bindings.
- If you add offline caching, extend `MusicRepository` with a data source layer and fallbacks.

**Summary**
Keep ViewModels simple, mock MediaStore for tests, and wire `HomeScreen` via `MainActivity` using a small factory that provides `MusicLoader`, `MusicRepository`, and `PlayerController`. Use `./gradlew assembleDebug` to validate a local build.

--
Generated/updated for AI agent consumption on May 07, 2026