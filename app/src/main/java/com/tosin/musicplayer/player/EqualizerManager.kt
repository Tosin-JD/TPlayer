package com.tosin.musicplayer.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.tosin.musicplayer.ui.state.EqBand
import com.tosin.musicplayer.ui.state.EqualizerPresetUi
import com.tosin.musicplayer.ui.state.EqualizerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

object EqualizerManager {
    private const val TAG = "EqualizerManager"
    private const val CUSTOM_PRESET_ID = "custom"

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var currentPresetId: String = "flat"

    private val presetDefinitions = listOf(
        PresetDefinition(
            id = "flat",
            name = "Flat",
            description = "Balanced sound with no coloration.",
            bandCurveDb = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        ),
        PresetDefinition(
            id = "bass_boost",
            name = "Bass Boost",
            description = "Heavy low-end with a soft top end.",
            bandCurveDb = listOf(7f, 6f, 5f, 3f, 1f, 0f, -1f, -1f, 0f, 1f),
            bassBoost = 800,
            loudness = 250
        ),
        PresetDefinition(
            id = "treble_boost",
            name = "Treble Boost",
            description = "Bright highs with a leaner low end.",
            bandCurveDb = listOf(-3f, -2f, -1f, 0f, 1f, 2f, 4f, 5f, 6f, 7f),
            loudness = 150
        ),
        PresetDefinition(
            id = "rock",
            name = "Rock",
            description = "Punchy lows and crisp highs.",
            bandCurveDb = listOf(5f, 4f, 2f, 0f, -1f, 0f, 2f, 4f, 5f, 6f),
            bassBoost = 350,
            loudness = 200
        ),
        PresetDefinition(
            id = "pop",
            name = "Pop",
            description = "Clear vocals and radio-friendly sparkle.",
            bandCurveDb = listOf(0f, 2f, 4f, 5f, 4f, 2f, 1f, 0f, 1f, 3f),
            bassBoost = 200,
            virtualizer = 150
        ),
        PresetDefinition(
            id = "jazz",
            name = "Jazz",
            description = "Warm mids with a relaxed top end.",
            bandCurveDb = listOf(0f, 1f, 2f, 3f, 2f, 0f, 1f, 2f, 3f, 2f),
            virtualizer = 120
        ),
        PresetDefinition(
            id = "classical",
            name = "Classical",
            description = "Wide, balanced playback for orchestral tracks.",
            bandCurveDb = listOf(3f, 2f, 1f, 0f, -1f, -1f, 0f, 1f, 2f, 3f)
        ),
        PresetDefinition(
            id = "hip_hop",
            name = "Hip Hop",
            description = "Hard bass with a clean vocal pocket.",
            bandCurveDb = listOf(7f, 6f, 5f, 2f, 0f, -1f, 0f, 2f, 4f, 5f),
            bassBoost = 900,
            loudness = 300
        ),
        PresetDefinition(
            id = "electronic",
            name = "Electronic / Dance",
            description = "Energetic punch with a sparkling top end.",
            bandCurveDb = listOf(6f, 5f, 3f, 1f, -1f, 1f, 3f, 5f, 7f, 6f),
            bassBoost = 650,
            virtualizer = 300,
            loudness = 350
        ),
        PresetDefinition(
            id = "vocal",
            name = "Vocal",
            description = "Brings the voice forward and cleans the mix.",
            bandCurveDb = listOf(-2f, -1f, 0f, 3f, 5f, 6f, 4f, 2f, 1f, 0f),
            virtualizer = 80
        ),
        PresetDefinition(
            id = "loudness",
            name = "Loudness",
            description = "Fuller, louder playback at lower volumes.",
            bandCurveDb = listOf(4f, 4f, 3f, 2f, 1f, 1f, 2f, 3f, 4f, 4f),
            bassBoost = 250,
            loudness = 800
        ),
        PresetDefinition(
            id = "movie",
            name = "Movie / Cinema",
            description = "Wide dynamics for immersive movie sound.",
            bandCurveDb = listOf(5f, 4f, 2f, 0f, -1f, 0f, 2f, 4f, 5f, 6f),
            bassBoost = 300,
            virtualizer = 700,
            loudness = 450
        )
    )

    private val _uiState = MutableStateFlow(
        EqualizerUiState(
            presets = presetDefinitions.map { it.toUi() }
        )
    )
    val uiState = _uiState.asStateFlow()

    fun init(audioSessionId: Int) {
        if (audioSessionId == 0) return

        release()

        equalizer = createEqualizer(audioSessionId)
        bassBoost = createBassBoost(audioSessionId)
        virtualizer = createVirtualizer(audioSessionId)
        loudnessEnhancer = createLoudnessEnhancer(audioSessionId)

        val available = equalizer != null
        _uiState.value = _uiState.value.copy(
            isAvailable = available,
            presets = presetDefinitions.map { it.toUi() }
        )

        if (!available) {
            return
        }

        applyPreset(presetDefinitions.first())
        refreshBandsAndEffects()
    }

    fun setEnabled(enabled: Boolean) {
        if (!uiState.value.isAvailable) return
        _uiState.value = _uiState.value.copy(enabled = enabled)
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
        loudnessEnhancer?.enabled = enabled
    }

    fun setPreset(presetId: String) {
        val preset = presetDefinitions.firstOrNull { it.id == presetId } ?: return
        if (!uiState.value.isAvailable) return
        applyPreset(preset)
        refreshBandsAndEffects()
    }

    fun setBandLevel(bandId: Int, level: Int) {
        val eq = equalizer ?: return
        val band = _uiState.value.bands.firstOrNull { it.id == bandId } ?: return
        val clampedLevel = level.coerceIn(band.minLevel, band.maxLevel)
        eq.setBandLevel(bandId.toShort(), clampedLevel.toShort())

        val updatedBands = _uiState.value.bands.map {
            if (it.id == bandId) it.copy(level = clampedLevel) else it
        }
        _uiState.value = _uiState.value.copy(
            bands = updatedBands,
            selectedPresetId = CUSTOM_PRESET_ID,
            selectedPresetName = "Custom",
            selectedPresetDescription = "Manual tuning based on your adjustments."
        )
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _uiState.value = _uiState.value.copy(bassBoost = clamped)
        bassBoost?.setStrength(clamped.toShort())
        markCustomIfNeeded()
    }

    fun setVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _uiState.value = _uiState.value.copy(virtualizer = clamped)
        virtualizer?.setStrength(clamped.toShort())
        markCustomIfNeeded()
    }

    fun setLoudness(gain: Int) {
        val clamped = gain.coerceIn(0, 2000)
        _uiState.value = _uiState.value.copy(loudness = clamped)
        loudnessEnhancer?.setTargetGain(clamped)
        markCustomIfNeeded()
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()

        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }

    private fun createEqualizer(audioSessionId: Int): Equalizer? = runCatching {
        Equalizer(0, audioSessionId).apply {
            enabled = _uiState.value.enabled
        }
    }.getOrElse {
        Log.e(TAG, "Equalizer unavailable", it)
        null
    }

    private fun createBassBoost(audioSessionId: Int): BassBoost? = runCatching {
        BassBoost(0, audioSessionId).apply {
            enabled = _uiState.value.enabled
        }
    }.getOrElse {
        Log.w(TAG, "BassBoost unavailable", it)
        null
    }

    private fun createVirtualizer(audioSessionId: Int): Virtualizer? = runCatching {
        Virtualizer(0, audioSessionId).apply {
            enabled = _uiState.value.enabled
        }
    }.getOrElse {
        Log.w(TAG, "Virtualizer unavailable", it)
        null
    }

    private fun createLoudnessEnhancer(audioSessionId: Int): LoudnessEnhancer? = runCatching {
        LoudnessEnhancer(audioSessionId).apply {
            enabled = _uiState.value.enabled
        }
    }.getOrElse {
        Log.w(TAG, "LoudnessEnhancer unavailable", it)
        null
    }

    private fun applyPreset(preset: PresetDefinition) {
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        val minLevel = range[0].toInt()
        val maxLevel = range[1].toInt()
        val bandCount = eq.numberOfBands.toInt()
        val bands = (0 until bandCount).map { band ->
            val normalizedPosition = if (bandCount <= 1) {
                0f
            } else {
                band.toFloat() / (bandCount - 1).toFloat()
            }
            val levelDb = interpolateCurve(preset.bandCurveDb, normalizedPosition)
            val levelMb = (levelDb * 100f).roundToInt().coerceIn(minLevel, maxLevel)
            eq.setBandLevel(band.toShort(), levelMb.toShort())
            EqBand(
                id = band,
                frequency = eq.getCenterFreq(band.toShort()) / 1000,
                level = levelMb,
                minLevel = minLevel,
                maxLevel = maxLevel
            )
        }

        currentPresetId = preset.id
        _uiState.value = _uiState.value.copy(
            bands = bands,
            selectedPresetId = preset.id,
            selectedPresetName = preset.name,
            selectedPresetDescription = preset.description,
            bassBoost = preset.bassBoost.coerceIn(0, 1000),
            virtualizer = preset.virtualizer.coerceIn(0, 1000),
            loudness = preset.loudness.coerceIn(0, 2000)
        )

        bassBoost?.setStrength(_uiState.value.bassBoost.toShort())
        virtualizer?.setStrength(_uiState.value.virtualizer.toShort())
        loudnessEnhancer?.setTargetGain(_uiState.value.loudness)
    }

    private fun refreshBandsAndEffects() {
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        val minLevel = range[0].toInt()
        val maxLevel = range[1].toInt()
        val bandCount = eq.numberOfBands.toInt()
        val bands = (0 until bandCount).map { band ->
            val levelMb = eq.getBandLevel(band.toShort()).toInt()
            EqBand(
                id = band,
                frequency = eq.getCenterFreq(band.toShort()) / 1000,
                level = levelMb,
                minLevel = minLevel,
                maxLevel = maxLevel
            )
        }

        _uiState.value = _uiState.value.copy(bands = bands)
    }

    private fun markCustomIfNeeded() {
        if (currentPresetId == CUSTOM_PRESET_ID) return
        _uiState.value = _uiState.value.copy(
            selectedPresetId = CUSTOM_PRESET_ID,
            selectedPresetName = "Custom",
            selectedPresetDescription = "Manual tuning based on your adjustments."
        )
        currentPresetId = CUSTOM_PRESET_ID
    }

    private fun interpolateCurve(curve: List<Float>, position: Float): Float {
        if (curve.isEmpty()) return 0f
        if (curve.size == 1) return curve.first()

        val clampedPosition = position.coerceIn(0f, 1f)
        val scaled = clampedPosition * (curve.size - 1)
        val startIndex = scaled.toInt().coerceIn(0, curve.lastIndex)
        val endIndex = (startIndex + 1).coerceAtMost(curve.lastIndex)
        val fraction = scaled - startIndex
        val start = curve[startIndex]
        val end = curve[endIndex]
        return start + (end - start) * fraction
    }

    private fun PresetDefinition.toUi() = EqualizerPresetUi(
        id = id,
        name = name,
        description = description
    )

    private data class PresetDefinition(
        val id: String,
        val name: String,
        val description: String,
        val bandCurveDb: List<Float>,
        val bassBoost: Int = 0,
        val virtualizer: Int = 0,
        val loudness: Int = 0
    )
}
