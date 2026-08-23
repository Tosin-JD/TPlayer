package com.tosin.musicplayer.player

import androidx.media3.common.Player
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

@OptIn(ExperimentalCoroutinesApi::class)
class CrossfadeManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var crossfadeManager: CrossfadeManager

    @Before
    fun setUp() {
        crossfadeManager = CrossfadeManager(scope = testScope)
        crossfadeManager.enabled = true
        crossfadeManager.configuredDurationMs = 4000L
    }

    @Test
    fun `equal power volume curve maintains sum of power around 1`() {
        val testPoints = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        for (t in testPoints) {
            val (vOut, vIn) = crossfadeManager.calculateEqualPowerVolumes(t)
            val powerSum = (vOut * vOut) + (vIn * vIn)
            assertTrue("Power sum at t=$t should be approximately 1.0 (was $powerSum)", abs(powerSum - 1.0f) < 0.01f)
        }
    }

    @Test
    fun `equal power curve midpoint volume is cos(pi by 4) approx 0_707`() {
        val (vOut, vIn) = crossfadeManager.calculateEqualPowerVolumes(0.5f)
        assertTrue("vOut at 50% should be ~0.707", abs(vOut - 0.7071f) < 0.01f)
        assertTrue("vIn at 50% should be ~0.707", abs(vIn - 0.7071f) < 0.01f)
    }

    @Test
    fun `getEffectiveDurationMs caps at half of song duration`() {
        crossfadeManager.configuredDurationMs = 5000L

        // Song duration 4000ms -> capped at 2000ms
        assertEquals(2000L, crossfadeManager.getEffectiveDurationMs(4000L))

        // Song duration 20000ms -> capped at configured 5000ms
        assertEquals(5000L, crossfadeManager.getEffectiveDurationMs(20000L))

        // Invalid song duration -> 0
        assertEquals(0L, crossfadeManager.getEffectiveDurationMs(0L))
    }

    @Test
    fun `shouldTriggerCrossfade returns true when in fade zone`() {
        crossfadeManager.configuredDurationMs = 4000L
        val trackDurationMs = 10000L // effective fade = 4000ms

        // Position 5000ms -> remaining 5000ms > effective 4000ms -> false
        assertFalse(crossfadeManager.shouldTriggerCrossfade(5000L, trackDurationMs))

        // Position 7000ms -> remaining 3000ms <= effective 4000ms -> true
        assertTrue(crossfadeManager.shouldTriggerCrossfade(7000L, trackDurationMs))

        // Disabled -> false
        crossfadeManager.enabled = false
        assertFalse(crossfadeManager.shouldTriggerCrossfade(7000L, trackDurationMs))
    }

    @Test
    fun `startCrossfade smoothly transition volumes over time`() = testScope.runTest {
        val activePlayer = mockk<Player>(relaxed = true) {
            every { isPlaying } returns true
        }
        val incomingPlayer = mockk<Player>(relaxed = true) {
            every { isPlaying } returns true
        }

        var completed = false
        crossfadeManager.startCrossfade(
            activePlayer = activePlayer,
            incomingPlayer = incomingPlayer,
            durationMs = 1000L,
            tickIntervalMs = 200L
        ) {
            completed = true
        }

        assertTrue(crossfadeManager.isCrossfading)

        // Advance 500ms (midpoint)
        advanceTimeBy(500L)
        verify { activePlayer.volume = any() }
        verify { incomingPlayer.volume = any() }

        // Advance past completion
        advanceTimeBy(600L)
        assertFalse(crossfadeManager.isCrossfading)
        assertTrue(completed)
        verify { activePlayer.volume = 0.0f }
        verify { activePlayer.pause() }
        verify { incomingPlayer.volume = 1.0f }
    }

    @Test
    fun `cancelCrossfade immediately halts fade and resets volumes`() = testScope.runTest {
        val activePlayer = mockk<Player>(relaxed = true)
        val incomingPlayer = mockk<Player>(relaxed = true) {
            every { isPlaying } returns true
        }

        crossfadeManager.startCrossfade(
            activePlayer = activePlayer,
            incomingPlayer = incomingPlayer,
            durationMs = 2000L
        )

        advanceTimeBy(500L)
        assertTrue(crossfadeManager.isCrossfading)

        crossfadeManager.cancelCrossfade(
            activePlayer = activePlayer,
            fadingOutPlayer = incomingPlayer,
            resetActiveVolume = true
        )

        assertFalse(crossfadeManager.isCrossfading)
        verify { incomingPlayer.volume = 0.0f }
        verify { incomingPlayer.pause() }
        verify { activePlayer.volume = 1.0f }
    }
}
