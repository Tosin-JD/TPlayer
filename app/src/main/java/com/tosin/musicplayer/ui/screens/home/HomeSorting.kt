package com.tosin.musicplayer.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.state.LibraryGroup
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.state.matchesStorageScope

internal val LibrarySortMapSaver =
    Saver<SnapshotStateMap<LibraryTab, LibrarySortOption>, Map<LibraryTab, LibrarySortOption>>(
        save = { map -> LinkedHashMap(map) },
        restore = { saved ->
            mutableStateMapOf<LibraryTab, LibrarySortOption>().apply { putAll(saved) }
        }
    )

@Composable
fun rememberLibrarySortState(): SnapshotStateMap<LibraryTab, LibrarySortOption> =
    rememberSaveable(saver = LibrarySortMapSaver) {
        mutableStateMapOf()
    }

fun sortSongs(
    songs: List<Song>,
    sortBy: LibrarySortOption
): List<Song> {
    return when (sortBy) {
        LibrarySortOption.TitleAz -> songs.sortedBy { it.title.trim().lowercase() }
        LibrarySortOption.ArtistAz -> songs.sortedBy { it.artist.trim().lowercase() }
        LibrarySortOption.AlbumAz -> songs.sortedBy { it.album.trim().lowercase() }
        LibrarySortOption.Genre -> songs.sortedBy { it.genre.orEmpty().trim().lowercase() }
        LibrarySortOption.ReleaseYear -> songs.sortedByDescending { it.year ?: 0 }
        LibrarySortOption.Duration -> songs.sortedByDescending { it.duration }
        LibrarySortOption.TrackNumber -> songs.sortedBy { it.trackNumber }
        LibrarySortOption.PopularityPlays -> songs.sortedByDescending { it.playCount }
        LibrarySortOption.DateAdded -> songs.sortedByDescending { it.dateAddedMs ?: 0L }
        LibrarySortOption.Rating -> songs.sortedByDescending { it.rating ?: 0 }
        LibrarySortOption.RecentlyPlayed -> songs.sortedByDescending { it.lastPlayedMs ?: 0L }
        LibrarySortOption.FileSize -> songs.sortedByDescending { it.fileSizeBytes ?: 0L }
    }
}

fun sortLibraryGroups(
    groups: List<LibraryGroup>,
    sortBy: LibrarySortOption
): List<LibraryGroup> {
    return when (sortBy) {
        LibrarySortOption.TitleAz -> groups.sortedBy { it.title.trim().lowercase() }
        LibrarySortOption.ArtistAz -> groups.sortedBy { it.songs.firstOrNull()?.artist.orEmpty().trim().lowercase() }
        LibrarySortOption.AlbumAz -> groups.sortedBy { it.songs.firstOrNull()?.album.orEmpty().trim().lowercase() }
        LibrarySortOption.Genre -> groups.sortedBy { it.title.trim().lowercase() }
        LibrarySortOption.ReleaseYear -> groups.sortedByDescending { it.songs.maxOfOrNull { song -> song.year ?: 0 } ?: 0 }
        LibrarySortOption.Duration -> groups.sortedByDescending { it.songs.sumOf { song -> song.duration } }
        LibrarySortOption.TrackNumber -> groups.sortedBy { it.songs.minOfOrNull { song -> song.trackNumber } ?: Int.MAX_VALUE }
        LibrarySortOption.PopularityPlays -> groups.sortedByDescending { it.songs.sumOf { song -> song.playCount } }
        LibrarySortOption.DateAdded -> groups.sortedByDescending { it.songs.maxOfOrNull { song -> song.dateAddedMs ?: 0L } ?: 0L }
        LibrarySortOption.Rating -> groups.sortedByDescending {
            val ratings = it.songs.mapNotNull { song -> song.rating }
            if (ratings.isEmpty()) 0.0 else ratings.average()
        }
        LibrarySortOption.RecentlyPlayed -> groups.sortedByDescending { it.songs.maxOfOrNull { song -> song.lastPlayedMs ?: 0L } ?: 0L }
        LibrarySortOption.FileSize -> groups.sortedByDescending { it.songs.sumOf { song -> song.fileSizeBytes ?: 0L } }
    }
}

fun filterGroupsForStorage(
    groups: List<LibraryGroup>,
    storageScope: StorageScope
): List<LibraryGroup> {
    return groups.mapNotNull { group ->
        val filteredSongs = group.songs.filter { it.matchesStorageScope(storageScope) }
        if (filteredSongs.isEmpty()) null
        else group.copy(songs = filteredSongs, songCount = filteredSongs.size)
    }
}
