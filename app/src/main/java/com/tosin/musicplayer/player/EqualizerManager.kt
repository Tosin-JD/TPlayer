package com.tosin.musicplayer.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.tosin.musicplayer.ui.state.EqBand
import com.tosin.musicplayer.ui.state.EqualizerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object EqualizerManager {
    private const val TAG = "EqualizerManager"

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState = _uiState.asStateFlow()

    fun init(audioSessionId: Int) {
        if (audioSessionId == 0) return
        
        try {
            release()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _uiState.value.enabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _uiState.value.enabled
                setStrength(_uiState.value.bassBoost.toShort())
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _uiState.value.enabled
                setStrength(_uiState.value.virtualizer.toShort())
            }
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = _uiState.value.enabled
                setTargetGain(_uiState.value.loudness)
            }

            loadInitialState()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Equalizer", e)
        }
    }

    private fun loadInitialState() {
        val eq = equalizer ?: return
        val bandsCount = eq.numberOfBands
        val minLevel = eq.bandLevelRange[0].toInt()
        val maxLevel = eq.bandLevelRange[1].toInt()

        val bands = (0 until bandsCount).map { i ->
            EqBand(
                id = i,
                frequency = eq.getCenterFreq(i.toShort()) / 1000,
                level = eq.getBandLevel(i.toShort()).toInt(),
                minLevel = minLevel,
                maxLevel = maxLevel
            )
        }

        val presets = (0 until eq.numberOfPresets).map { i ->
            eq.getPresetName(i.toShort())
        }

        _uiState.value = _uiState.value.copy(
            bands = bands,
            presets = presets,
            selectedPreset = eq.currentPreset.toInt()
        )
    }

    fun setEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enabled = enabled)
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
        loudnessEnhancer?.enabled = enabled
    }

    fun setBandLevel(bandId: Int, level: Int) {
        equalizer?.setBandLevel(bandId.toShort(), level.toShort())
        val updatedBands = _uiState.value.bands.map {
            if (it.id == bandId) it.copy(level = level) else it
        }
        _uiState.value = _uiState.value.copy(bands = updatedBands, selectedPreset = -1)
    }

    fun setPreset(presetIndex: Int) {
        equalizer?.usePreset(presetIndex.toShort())
        loadInitialState() // Reload bands levels after preset change
        _uiState.value = _uiState.value.copy(selectedPreset = presetIndex)
    }

    fun setBassBoost(strength: Int) {
        _uiState.value = _uiState.value.copy(bassBoost = strength)
        bassBoost?.setStrength(strength.toShort())
    }

    fun setVirtualizer(strength: Int) {
        _uiState.value = _uiState.value.copy(virtualizer = strength)
        virtualizer?.setStrength(strength.toShort())
    }

    fun setLoudness(gain: Int) {
        _uiState.value = _uiState.value.copy(loudness = gain)
        loudnessEnhancer?.setTargetGain(gain)
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
}
