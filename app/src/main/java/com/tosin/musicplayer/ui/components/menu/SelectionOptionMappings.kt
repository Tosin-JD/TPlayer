package com.tosin.musicplayer.ui.components.menu

import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.SortBy
import com.tosin.musicplayer.ui.state.StatsRange

fun librarySortOptions(): List<SelectionOption<LibrarySortOption>> =
    LibrarySortOption.entries.map { option ->
        SelectionOption(
            value = option,
            label = option.label
        )
    }

fun statsSortOptions(): List<SelectionOption<SortBy>> =
    SortBy.entries.map { option ->
        SelectionOption(
            value = option,
            label = option.label
        )
    }

fun statsRangeOptions(): List<SelectionOption<StatsRange>> =
    StatsRange.entries.map { option ->
        SelectionOption(
            value = option,
            label = option.label
        )
    }
