package com.tosin.musicplayer.ui.viewmodel

import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.models.SongStats
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.data.repository.StatsRepository
import com.tosin.musicplayer.ui.state.SortBy
import com.tosin.musicplayer.ui.state.StatsRange
import com.tosin.musicplayer.ui.state.startTimeForRange
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private fun song(id: Long) = Song(
        id = id,
        title = "Song $id",
        artist = "Artist",
        album = "Album",
        genre = null,
        folder = null,
        uri = "content://song/$id",
        albumArt = null,
        duration = 0L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectRange updates the selectedRange flow`() = runTest(dispatcher) {
        val viewModel = StatsViewModel(mockRepository(), mockk())

        viewModel.selectRange(StatsRange.ThisWeek)

        assertEquals(StatsRange.ThisWeek, viewModel.selectedRange.value)
    }

    @Test
    fun `selectSort updates the sortBy flow`() = runTest(dispatcher) {
        val viewModel = StatsViewModel(mockRepository(), mockk())

        viewModel.selectSort(SortBy.DURATION)

        assertEquals(SortBy.DURATION, viewModel.sortBy.value)
    }

    @Test
    fun `mostPlayed loads from repository and reloads when the range changes`() =
        runTest(dispatcher) {
            val songs = listOf(song(1), song(2))
            val repository = mockk<MusicRepository>()
            val statsRepository = mockk<StatsRepository>()

            every { repository.getSongs() } returns flowOf(songs)
            coEvery { statsRepository.getMostPlayed(any(), any()) } returns emptyList()

            val viewModel = StatsViewModel(repository, statsRepository)
            collectInBackground(viewModel)

            coVerify(exactly = 1) {
                statsRepository.getMostPlayed(songs, startTimeForRange(StatsRange.AllTime))
            }

            viewModel.selectRange(StatsRange.ThisWeek)
            advanceUntilIdle()

            coVerify {
                statsRepository.getMostPlayed(songs, startTimeForRange(StatsRange.ThisWeek))
            }
        }

    @Test
    fun `mostPlayed exposes the repository result`() = runTest(dispatcher) {
        val songs = listOf(song(1))
        val stat = SongStats(song = songs[0], playCount = 3, totalMinutes = 5)
        val repository = mockk<MusicRepository>()
        val statsRepository = mockk<StatsRepository>()

        every { repository.getSongs() } returns flowOf(songs)
        coEvery { statsRepository.getMostPlayed(any(), any()) } returns listOf(stat)

        val viewModel = StatsViewModel(repository, statsRepository)
        backgroundScope.launch(dispatcher) { viewModel.mostPlayed.collect {} }
        advanceUntilIdle()

        coVerify(exactly = 1) {
            statsRepository.getMostPlayed(any<List<Song>>(), any<Long>())
        }

        assertEquals(listOf(stat), viewModel.mostPlayed.value)
    }

    private fun mockRepository(): MusicRepository {
        val repository = mockk<MusicRepository>()
        every { repository.getSongs() } returns flowOf(emptyList())
        return repository
    }

    private fun TestScope.collectInBackground(viewModel: StatsViewModel) {
        backgroundScope.launch(dispatcher) {
            viewModel.mostPlayed.collect {}
        }
        advanceUntilIdle()
    }
}
