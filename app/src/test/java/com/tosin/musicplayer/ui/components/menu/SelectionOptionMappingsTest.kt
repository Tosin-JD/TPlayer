package com.tosin.musicplayer.ui.components.menu

import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.SortBy
import com.tosin.musicplayer.ui.state.StatsRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SelectionOptionMappingsTest {

    @Test
    fun `librarySortOptions covers every sort option in declaration order`() {
        val options = librarySortOptions()

        assertEquals(LibrarySortOption.entries.size, options.size)
        assertEquals(LibrarySortOption.entries.toList(), options.map { it.value })
        assertEquals(LibrarySortOption.entries.map { it.label }, options.map { it.label })
    }

    @Test
    fun `librarySortOptions rows are non-destructive and carry no icons`() {
        librarySortOptions().forEach { option ->
            assertNull(option.icon)
            assertFalse(option.destructive)
        }
    }

    @Test
    fun `statsSortOptions covers both sort modes`() {
        val options = statsSortOptions()

        assertEquals(listOf(SortBy.PLAY_COUNT, SortBy.DURATION), options.map { it.value })
        assertEquals(listOf("By Plays", "By Minutes"), options.map { it.label })
    }

    @Test
    fun `statsRangeOptions covers every range in declaration order`() {
        val options = statsRangeOptions()

        assertEquals(StatsRange.entries.toList(), options.map { it.value })
        assertEquals(
            listOf("Today", "This Week", "This Month", "This Year", "All Time"),
            options.map { it.label }
        )
    }
}
