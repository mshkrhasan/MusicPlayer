# Project Plan

Refine the existing Music Player app to have a premium, luxury UI and specific functional improvements.
Key updates:
1. Home Screen: 3-tab layout (All Music, Audio Folders, Video Folders).
2. Now Playing UI: Rotating disc animation for missing thumbnails.
3. Media3 Service: Swipe-to-stop notification behavior.
4. Luxury aesthetic with Material 3 and Glassmorphism.

## Project Brief

# Music Player - Project Brief

## Features
*   **Luxury 3-Tab Home:** A premium media library interface featuring organized tabs for "All Music" (flat list), "Folders" (audio by directory), and "Video Folders" (audio-from-video browsing).
*   **Immersive Playback Experience:** A high-end 'Now Playing' screen utilizing glassmorphism, dynamic Material 3 colors, and a signature rotating disc animation for tracks missing album art.
*   **Advanced Media3 Service:** Seamless background playback powered by Media3 ExoPlayer, ensuring music continues while navigating the app or using other tools.
*   **Responsive Playback Control:** A custom media notification that supports "swipe-to-stop" behavior, instantly terminating the player and service when dismissed.

## High-Level Technical Stack
*   **Language:** Kotlin
*   **Asynchronous Framework:** Kotlin Coroutines & Flow
*   **UI Framework:** Jetpack Compose (Material Design 3)
*   **Navigation Strategy:** **Jetpack Navigation 3** (state-driven navigation model)
*   **Adaptive Strategy:** **Compose Material Adaptive** library for responsive, multi-pane layouts across different screen sizes.
*   **Media Handling:** Media3 ExoPlayer & MediaSession (supporting background service and custom notification dismissal logic).

## Implementation Steps

### Task_1_Setup_Infrastructure: Set up the core infrastructure including Hilt for DI, Room for persistence, and Media3 for playback.
- **Status:** COMPLETED
- **Updates:** Hilt, Room, and Media3 dependencies integrated. MusicPlayerApp class and MainActivity configured for Hilt. Room Database with Song and Playlist entities implemented. PlaybackService and MediaModule for Media3 ExoPlayer setup. MusicRepository for MediaStore scanning and playlist management created. Project builds successfully.
- **Acceptance Criteria:**
  - Hilt, Room, and Media3 dependencies added and synced
  - Room Database and Entities defined for Playlists/Favorites
  - Media3 Service and MediaSession skeleton implemented

### Task_2_Media_Scanning_Library_UI: Implement local media scanning and the main Library UI components.
- **Status:** COMPLETED
- **Updates:** Media scanning for audio and video files implemented in MusicRepository. Library ViewModel created to handle search, sort, and media states. LibraryScreen built using Jetpack Compose and Material 3 with SearchBar and LazyColumn. Jetpack Navigation 3 integrated for state-driven navigation. Runtime permission handling for storage implemented using Accompanist. Edge-to-edge layout configured. Project builds successfully.
- **Acceptance Criteria:**
  - Media scanner fetches audio and video files from storage
  - Library, Albums, and Artists screens implemented with M3
  - Search and Sort functionality working in the library
  - App handles storage permissions correctly

### Task_3_Playback_Engine_NowPlaying: Implement full playback logic and the immersive 'Now Playing' screen.
- **Status:** COMPLETED
- **Updates:** Playback logic implemented using Media3 ExoPlayer and MediaSession. PlayerViewModel created to manage playback state and controls. Now Playing screen built with glassmorphism (blur effect) and dynamic colors extracted from album art using Palette API. Mini player implemented for persistent control across screens. Background playback and notification controls fully functional. Jetpack Navigation 3 used for routing between Library and Now Playing. Project builds successfully.
- **Acceptance Criteria:**
  - ExoPlayer handles play/pause, seek, shuffle, and repeat
  - Now Playing screen features glassmorphism and dynamic colors from album art
  - Mini player implemented for persistent control
  - Background playback and notification controls functional

### Task_4_Playlists_VideoToAudio_Settings: Implement playlist management, video-to-audio mode, and app settings.
- **Status:** COMPLETED
- **Updates:** Playlist management implemented with Room database for creation, editing, and adding/removing songs. "Favorites" functionality fully operational. Video-to-Audio toggle added to Settings, allowing video files to appear in the Library with a badge and play as audio. Settings screen supports Light, Dark, and AMOLED (true black) themes using Preferences DataStore. UI updated to include video badges in the library list. Project builds successfully.
- **Acceptance Criteria:**
  - Users can create/edit playlists and add to favorites
  - Video-to-Audio toggle includes video files in the library
  - Settings screen allows switching between Light, Dark, and AMOLED modes

### Task_5_Refine_Luxury_UI_And_Service: Implement luxury UI refinements and advanced Media3 behaviors including a 3-tab home layout and rotating disc animation.
- **Status:** COMPLETED
- **Updates:** Implemented luxury 3-tab home layout (All Music, Folders, Video Folders). Added rotating disc animation for missing album art in Now Playing screen. Updated Media3 PlaybackService with onTaskRemoved logic to support swipe-to-stop/notification dismissal behavior. Refined UI with glassmorphism and Material 3 for a premium feel. Project builds successfully.
- **Acceptance Criteria:**
  - Home screen features 3-tab layout: All Music, Audio Folders, Video Folders
  - Now Playing UI displays a rotating disc animation for missing thumbnails
  - Media3 service updated to support swipe-to-stop notification behavior
  - Luxury aesthetic with glassmorphism and Material 3 refined throughout

### Task_6_Run_And_Verify: Final run and verification of the application stability and alignment with luxury UI requirements.
- **Status:** COMPLETED
- **Updates:** Audited and refined the entire application for a 'luxury' and 'perfect' UI. Redesigned PlaylistScreen with glassmorphism and premium typography. Re-engineered the rotating disc animation to look like a high-fidelity vinyl record. Verified the 3-tab home implementation (All Music, Folders, Video Folders). Ensured swipe-to-stop behavior in Media3 PlaybackService is fully functional. Fixed app launcher and icon issues in the manifest. Applied edge-to-edge fixes and state persistence using rememberSaveable. Project builds successfully and is ready for use.
- **Acceptance Criteria:**
  - Project builds successfully and app does not crash
  - All features (3-tab home, rotating disc, swipe-to-stop) verified
  - Verify alignment with luxury/premium aesthetic requirements
  - Make sure all existing tests pass
- **Duration:** N/A

