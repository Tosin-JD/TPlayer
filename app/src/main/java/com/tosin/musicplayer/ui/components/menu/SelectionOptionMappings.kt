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

fun repeatModeOptions(): List<SelectionOption<com.tosin.musicplayer.ui.viewmodel.RepeatMode>> =
    listOf(
        SelectionOption(
            value = com.tosin.musicplayer.ui.viewmodel.RepeatMode.PLAY_ALL_ONCE,
            label = "Play all once",
            icon = com.tosin.musicplayer.ui.icons.AppIcons.ParallelRightArrows
        ),
        SelectionOption(
            value = com.tosin.musicplayer.ui.viewmodel.RepeatMode.PLAY_ONE_ONCE,
            label = "Play one once",
            icon = com.tosin.musicplayer.ui.icons.AppIcons.LooksOne
        ),
        SelectionOption(
            value = com.tosin.musicplayer.ui.viewmodel.RepeatMode.REPEAT_ALL,
            label = "Play all on repeat",
            icon = com.tosin.musicplayer.ui.icons.AppIcons.Repeat
        ),
        SelectionOption(
            value = com.tosin.musicplayer.ui.viewmodel.RepeatMode.REPEAT_ONE,
            label = "Play one on repeat",
            icon = com.tosin.musicplayer.ui.icons.AppIcons.RepeatOne
        )
    )

