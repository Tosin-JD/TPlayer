package com.tosin.musicplayer.ui.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.icons.AppIcons

enum class LibraryTab(val label: String) {
    All("All"),
    Favorites("Favorites"),
    Album("Album"),
    Genre("Genre"),
    Folder("Folder"),
    Artist("Artist");

    fun icon(): ImageVector = when (this) {
        All -> AppIcons.MusicNote
        Favorites -> AppIcons.Favorite
        Album -> AppIcons.Album
        Genre -> AppIcons.Label
        Folder -> AppIcons.Folder
        Artist -> AppIcons.Person
    }
}

enum class LibrarySortOption(val label: String) {
    TitleAz("Title (A-Z)"),
    TitleZa("Title (Z-A)"),
    ArtistAz("Artist (A-Z)"),
    ArtistZa("Artist (Z-A)"),
    AlbumAz("Album (A-Z)"),
    AlbumZa("Album (Z-A)"),
    Genre("Genre"),
    GenreZa("Genre (Z-A)"),
    ReleaseYear("Release Year"),
    ReleaseYearAsc("Release Year (Oldest)"),
    Duration("Duration"),
    DurationAsc("Duration (Shortest)"),
    TrackNumber("Track Number"),
    TrackNumberDesc("Track Number (Desc)"),
    PopularityPlays("Popularity / Plays"),
    DateAdded("Date Added"),
    DateAddedOldest("Date Added (Oldest)"),
    Rating("Rating"),
    RecentlyPlayed("Recently Played"),
    FileSize("File Size")
}

data class LibraryGroup(
    val id: String,
    val title: String,
    val subtitle: String,
    val songCount: Int,
    val artwork: String?,
    val songs: List<Song> = emptyList()
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val hasAudioPermission: Boolean = true,
    val selectedTab: LibraryTab = LibraryTab.All,
    val songs: List<Song> = emptyList(),
    val libraryGroups: List<LibraryGroup> = emptyList(),
    val tabOrder: List<LibraryTab> = listOf(
        LibraryTab.All,
        LibraryTab.Favorites,
        LibraryTab.Album,
        LibraryTab.Artist,
        LibraryTab.Genre,
        LibraryTab.Folder
    )
)
