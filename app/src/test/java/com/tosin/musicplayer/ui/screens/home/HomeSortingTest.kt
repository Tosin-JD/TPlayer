package com.tosin.musicplayer.ui.screens.home

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.state.LibraryGroup
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import java.util.LinkedHashMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSortingTest {

    private val saverScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    private fun <Original, Saveable : Any> Saver<Original, Saveable>.saveInScope(
        value: Original
    ): Saveable? = with(saverScope) { save(value) }

    private fun restoreNonNull(
        saved: Map<LibraryTab, LibrarySortOption>
    ): SnapshotStateMap<LibraryTab, LibrarySortOption> =
        requireNotNull(LibrarySortMapSaver.restore(saved))

    private fun song(
        id: Long,
        title: String,
        artist: String = "Artist",
        album: String = "Album",
        folderPath: String? = null,
        duration: Long = 0L,
        year: Int? = null,
        playCount: Int = 0,
        lastPlayedMs: Long? = null,
        dateAddedMs: Long? = null,
        trackNumber: Int = 0
    ) = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        genre = null,
        folder = null,
        folderPath = folderPath,
        uri = "content://song/$id",
        albumArt = null,
        duration = duration,
        year = year,
        trackNumber = trackNumber,
        playCount = playCount,
        lastPlayedMs = lastPlayedMs,
        dateAddedMs = dateAddedMs
    )

    private fun group(id: String, title: String, songs: List<Song>) = LibraryGroup(
        id = id,
        title = title,
        subtitle = "",
        songCount = songs.size,
        artwork = null,
        songs = songs
    )

    // ---- LibrarySortMapSaver round-trip (§13.1) ----

    @Test
    fun `saver persists an empty map`() {
        val empty = mutableStateMapOf<LibraryTab, LibrarySortOption>()

        val saved = LibrarySortMapSaver.saveInScope(empty)
        val restored = LibrarySortMapSaver.restore(requireNotNull(saved))

        assertTrue(requireNotNull(restored).isEmpty())
    }

    @Test
    fun `saver round-trips enum keys and values`() {
        val stateMap = mutableStateMapOf<LibraryTab, LibrarySortOption>().apply {
            put(LibraryTab.All, LibrarySortOption.TitleAz)
            put(LibraryTab.Album, LibrarySortOption.ReleaseYear)
            put(LibraryTab.Artist, LibrarySortOption.Rating)
        }

        val saved = LibrarySortMapSaver.saveInScope(stateMap)
        val restored = restoreNonNull(requireNotNull(saved))

        assertEquals(stateMap.toMap(), restored.toMap())
        assertEquals(LibrarySortOption.TitleAz, restored[LibraryTab.All])
        assertEquals(LibrarySortOption.ReleaseYear, restored[LibraryTab.Album])
        assertEquals(LibrarySortOption.Rating, restored[LibraryTab.Artist])
    }

    @Test
    fun `saver saves a plain LinkedHashMap`() {
        val stateMap = mutableStateMapOf<LibraryTab, LibrarySortOption>().apply {
            put(LibraryTab.All, LibrarySortOption.TitleAz)
        }

        val saved = LibrarySortMapSaver.saveInScope(stateMap)

        assertTrue(saved is LinkedHashMap<*, *>)
    }

    // ---- sortSongs ----

    @Test
    fun `sortSongs TitleAz is case-insensitive ascending`() {
        val songs = listOf(
            song(1, "Beta"),
            song(2, "alpha"),
            song(3, "Gamma")
        )

        val sorted = sortSongs(songs, LibrarySortOption.TitleAz)

        assertEquals(listOf("alpha", "Beta", "Gamma"), sorted.map { it.title })
    }

    @Test
    fun `sortSongs ArtistAz is case-insensitive ascending`() {
        val songs = listOf(
            song(1, "One", artist = "Zulu"),
            song(2, "Two", artist = "alpha"),
            song(3, "Three", artist = "Mike")
        )

        val sorted = sortSongs(songs, LibrarySortOption.ArtistAz)

        assertEquals(listOf("Two", "Three", "One"), sorted.map { it.title })
    }

    @Test
    fun `sortSongs ReleaseYear is descending`() {
        val songs = listOf(
            song(1, "Old", year = 1999),
            song(2, "New", year = 2024),
            song(3, "Middle", year = 2010)
        )

        val sorted = sortSongs(songs, LibrarySortOption.ReleaseYear)

        assertEquals(listOf("New", "Middle", "Old"), sorted.map { it.title })
    }

    @Test
    fun `sortSongs PopularityPlays is descending by play count`() {
        val songs = listOf(
            song(1, "Loved", playCount = 5),
            song(2, "Meh", playCount = 1),
            song(3, "Popular", playCount = 42)
        )

        val sorted = sortSongs(songs, LibrarySortOption.PopularityPlays)

        assertEquals(listOf("Popular", "Loved", "Meh"), sorted.map { it.title })
    }

    // ---- albumTrackComparator ----

    @Test
    fun `albumTrackComparator sorts by track number then title`() {
        val songs = listOf(
            song(1, "Bravo", trackNumber = 3),
            song(2, "Alpha", trackNumber = 1),
            song(3, "Charlie", trackNumber = 2),
            song(4, "Delta", trackNumber = 0)
        )

        val sorted = songs.sortedWith(albumTrackComparator())

        assertEquals(listOf("Alpha", "Charlie", "Bravo", "Delta"), sorted.map { it.title })
    }

    @Test
    fun `albumTrackComparator pushes unknown track numbers to end`() {
        val songs = listOf(
            song(1, "Unknown1", trackNumber = 0),
            song(2, "Known", trackNumber = 1),
            song(3, "Unknown2", trackNumber = 0)
        )

        val sorted = songs.sortedWith(albumTrackComparator())

        assertEquals("Known", sorted.first().title)
        assertEquals(listOf("Unknown1", "Unknown2"), sorted.drop(1).map { it.title })
    }

    @Test
    fun `albumTrackComparator sorts equal track numbers alphabetically`() {
        val songs = listOf(
            song(1, "Charlie", trackNumber = 1),
            song(2, "Alpha", trackNumber = 1),
            song(3, "Bravo", trackNumber = 1)
        )

        val sorted = songs.sortedWith(albumTrackComparator())

        assertEquals(listOf("Alpha", "Bravo", "Charlie"), sorted.map { it.title })
    }

    @Test
    fun `albumTrackComparator handles all unknown track numbers`() {
        val songs = listOf(
            song(1, "Zebra", trackNumber = 0),
            song(2, "Alpha", trackNumber = 0),
            song(3, "Mango", trackNumber = 0)
        )

        val sorted = songs.sortedWith(albumTrackComparator())

        assertEquals(listOf("Alpha", "Mango", "Zebra"), sorted.map { it.title })
    }

    // ---- sortSongs TrackNumber ----

    @Test
    fun `sortSongs TrackNumber uses albumTrackComparator`() {
        val songs = listOf(
            song(1, "Bravo", trackNumber = 3),
            song(2, "Alpha", trackNumber = 1),
            song(3, "Charlie", trackNumber = 2),
            song(4, "Delta", trackNumber = 0)
        )

        val sorted = sortSongs(songs, LibrarySortOption.TrackNumber)

        assertEquals(listOf("Alpha", "Charlie", "Bravo", "Delta"), sorted.map { it.title })
    }

    @Test
    fun `sortSongs TrackNumberDesc reverses albumTrackComparator`() {
        val songs = listOf(
            song(1, "Bravo", trackNumber = 3),
            song(2, "Alpha", trackNumber = 1),
            song(3, "Charlie", trackNumber = 2),
            song(4, "Delta", trackNumber = 0)
        )

        val sorted = sortSongs(songs, LibrarySortOption.TrackNumberDesc)

        assertEquals(listOf("Delta", "Bravo", "Charlie", "Alpha"), sorted.map { it.title })
    }

    // ---- sortLibraryGroups ----

    @Test
    fun `sortLibraryGroups TitleAz sorts groups by title`() {
        val groups = listOf(
            group("g2", "Zebra", listOf(song(1, "One"))),
            group("g1", "apple", listOf(song(2, "Two"))),
            group("g3", "Mango", listOf(song(3, "Three")))
        )

        val sorted = sortLibraryGroups(groups, LibrarySortOption.TitleAz)

        assertEquals(listOf("apple", "Mango", "Zebra"), sorted.map { it.title })
    }

    @Test
    fun `sortLibraryGroups Duration sorts by summed duration descending`() {
        val groups = listOf(
            group("g1", "Short", listOf(song(1, "A", duration = 100L))),
            group("g2", "Long", listOf(song(2, "B", duration = 300L), song(3, "C", duration = 200L)))
        )

        val sorted = sortLibraryGroups(groups, LibrarySortOption.Duration)

        assertEquals(listOf("Long", "Short"), sorted.map { it.title })
    }

    // ---- filterGroupsForStorage ----

    @Test
    fun `filterGroupsForStorage Internal keeps only internal songs and updates count`() {
        val groups = listOf(
            group(
                "g1",
                "Mixed",
                listOf(
                    song(1, "Internal", folderPath = "/storage/emulated/0/Music/A.mp3"),
                    song(2, "Sd", folderPath = "/storage/1234-ABCD/Music/B.mp3")
                )
            )
        )

        val filtered = filterGroupsForStorage(groups, StorageScope.Internal)

        assertEquals(1, filtered.size)
        assertEquals(1, filtered[0].songCount)
        assertEquals(listOf("Internal"), filtered[0].songs.map { it.title })
    }

    @Test
    fun `filterGroupsForStorage Both keeps every song`() {
        val groups = listOf(
            group(
                "g1",
                "Mixed",
                listOf(
                    song(1, "Internal", folderPath = "/storage/emulated/0/Music/A.mp3"),
                    song(2, "Sd", folderPath = "/storage/1234-ABCD/Music/B.mp3")
                )
            )
        )

        val filtered = filterGroupsForStorage(groups, StorageScope.Both)

        assertEquals(1, filtered.size)
        assertEquals(2, filtered[0].songCount)
    }

    @Test
    fun `filterGroupsForStorage drops groups left empty`() {
        val groups = listOf(
            group("g1", "AllSd", listOf(song(1, "Sd", folderPath = "/storage/1234-ABCD/Music/A.mp3")))
        )

        val filtered = filterGroupsForStorage(groups, StorageScope.Internal)

        assertEquals(0, filtered.size)
    }

    @Test
    fun `filterGroupsForStorage handles songs without a folder path as internal`() {
        val groups = listOf(
            group("g1", "NoPath", listOf(song(1, "Blank")))
        )

        val filtered = filterGroupsForStorage(groups, StorageScope.Internal)

        assertEquals(1, filtered.size)
        assertEquals(listOf("Blank"), filtered[0].songs.map { it.title })
    }
}
