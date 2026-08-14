package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.models.SongStats
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.data.repository.StatsRepository
import com.tosin.musicplayer.ui.state.SortBy
import com.tosin.musicplayer.ui.state.StatsRange
import com.tosin.musicplayer.ui.state.startTimeForRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    private val repository: MusicRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(StatsRange.AllTime)
    val selectedRange: StateFlow<StatsRange> = _selectedRange.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.PLAY_COUNT)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    val mostPlayed: StateFlow<List<SongStats>> = combine(
        repository.getSongs(),
        _selectedRange
    ) { songs, range ->
        statsRepository.getMostPlayed(songs, startTimeForRange(range))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectRange(range: StatsRange) {
        _selectedRange.value = range
    }

    fun selectSort(sort: SortBy) {
        _sortBy.value = sort
    }
}
