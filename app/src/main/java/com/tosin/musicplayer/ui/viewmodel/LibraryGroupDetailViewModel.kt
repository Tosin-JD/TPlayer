package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.models.matchesSearch
import com.tosin.musicplayer.ui.screens.home.sortSongs
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Dedicated ViewModel for [LibraryGroupDetailScreen].
 *
 * Manages search query, sort option, and exposes a derived [displaySongs] StateFlow
 * that is the filtered + sorted list of songs for the current group.
 * Delegates playback operations to [PlayerViewModel].
 */
class LibraryGroupDetailViewModel(
    private val playerViewModel: PlayerViewModel,
    private val tab: LibraryTab,
    private val groupTitle: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(defaultSortForTab(tab))
    val sortOption: StateFlow<LibrarySortOption> = _sortOption.asStateFlow()

    /**
     * The list of songs to display, derived from the group's songs
     * after applying search filter and sort order.
     */
    val displaySongs: StateFlow<List<Song>> = combine(
        playerViewModel.uiState,
        _searchQuery,
        _sortOption
    ) { state, query, sort ->
        val groupSongs = playerViewModel.getSongsForGroup(tab, groupTitle)
        val filtered = if (query.isBlank()) groupSongs
        else groupSongs.filter { it.matchesSearch(query) }
        sortSongs(filtered, sort)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOption(option: LibrarySortOption) {
        _sortOption.value = option
    }

    fun onSongClick(songs: List<Song>, index: Int) {
        playerViewModel.onSongClick(songs, index)
    }

    companion object {
        fun defaultSortForTab(tab: LibraryTab): LibrarySortOption = when (tab) {
            LibraryTab.Album -> LibrarySortOption.TrackNumber
            LibraryTab.Artist -> LibrarySortOption.AlbumAz
            LibraryTab.Folder -> LibrarySortOption.TitleAz
            LibraryTab.Genre -> LibrarySortOption.TitleAz
            else -> LibrarySortOption.TitleAz
        }
    }
}
