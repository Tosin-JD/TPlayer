package com.tosin.musicplayer.ui.state

import com.tosin.musicplayer.data.models.Song

enum class LibraryTab(val label: String) {
    All("All"),
    Album("Album"),
    Genre("Genre"),
    Folder("Folder"),
    Artist("Artist")
}

data class LibraryGroup(
    val id: String,
    val title: String,
    val subtitle: String,
    val songCount: Int,
    val artwork: String?
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val hasAudioPermission: Boolean = true,
    val selectedTab: LibraryTab = LibraryTab.All,
    val songs: List<Song> = emptyList(),
    val libraryGroups: List<LibraryGroup> = emptyList()
)
