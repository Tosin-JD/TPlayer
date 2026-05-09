package com.tosin.musicplayer.ui.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.tosin.musicplayer.data.models.Song

enum class LibraryTab(val label: String) {
    All("All"),
    Album("Album"),
    Genre("Genre"),
    Folder("Folder"),
    Artist("Artist");

    fun icon(): ImageVector = when (this) {
        All -> Icons.Rounded.MusicNote
        Album -> Icons.Rounded.Album
        Genre -> Icons.Rounded.Label
        Folder -> Icons.Rounded.Folder
        Artist -> Icons.Rounded.Person
    }
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
    val libraryGroups: List<LibraryGroup> = emptyList(),
    val tabOrder: List<LibraryTab> = listOf(
        LibraryTab.All,
        LibraryTab.Album,
        LibraryTab.Artist,
        LibraryTab.Genre,
        LibraryTab.Folder
    )
)
