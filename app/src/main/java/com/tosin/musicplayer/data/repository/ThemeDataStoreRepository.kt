package com.tosin.musicplayer.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.theme.engine.ThemeParameters
import com.tosin.musicplayer.ui.theme.engine.ThemeState
import com.tosin.musicplayer.ui.theme.engine.ThemeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Thread-safe persistence repository that stores active style selection
 * and independent per-style visual overrides.
 */
class ThemeDataStoreRepository(private val context: Context) {
    private val themeFile = File(context.filesDir, "custom_theme_config.json")

    suspend fun saveThemeState(state: ThemeState) = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("activeStyle", state.activeStyle.name)

            val configsObj = JSONObject()
            state.styleConfigs.forEach { (style, params) ->
                configsObj.put(style.name, params.toJson())
            }
            root.put("styleConfigs", configsObj)

            themeFile.writeText(root.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadThemeState(): ThemeState = withContext(Dispatchers.IO) {
        if (!themeFile.exists()) return@withContext ThemeState()
        try {
            val root = JSONObject(themeFile.readText())
            val activeStyleName = root.optString("activeStyle", ThemeStyle.MATERIAL_EXPRESSIVE.name)
            val activeStyle = ThemeStyle.entries.firstOrNull { it.name == activeStyleName }
                ?: ThemeStyle.MATERIAL_EXPRESSIVE

            val defaultState = ThemeState()
            val mutableConfigs = defaultState.styleConfigs.toMutableMap()

            val configsObj = root.optJSONObject("styleConfigs")
            if (configsObj != null) {
                configsObj.keys().forEach { styleKey ->
                    val style = ThemeStyle.entries.firstOrNull { it.name == styleKey }
                    if (style != null) {
                        val paramJson = configsObj.getJSONObject(styleKey)
                        val defaultParams = mutableConfigs[style] ?: ThemeParameters.materialExpressiveDefault()
                        mutableConfigs[style] = paramJson.toThemeParameters(default = defaultParams)
                    }
                }
            }

            ThemeState(
                activeStyle = activeStyle,
                styleConfigs = mutableConfigs
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ThemeState()
        }
    }

    private fun ThemeParameters.toJson(): JSONObject = JSONObject().apply {
        put("primaryColor", primaryColor.toArgb())
        put("secondaryColor", secondaryColor.toArgb())
        put("tertiaryColor", tertiaryColor.toArgb())
        put("backgroundColor", backgroundColor.toArgb())
        put("surfaceColor", surfaceColor.toArgb())
        put("surfaceVariantColor", surfaceVariantColor.toArgb())
        put("onPrimaryColor", onPrimaryColor.toArgb())
        put("onSurfaceColor", onSurfaceColor.toArgb())
        put("onBackgroundColor", onBackgroundColor.toArgb())
        put("cornerRadiusScale", cornerRadiusScale.toDouble())
        put("borderStrokeWidth", borderStrokeWidth.value.toDouble())
        put("borderColor", borderColor.toArgb())
        put("surfaceBlur", surfaceBlur.value.toDouble())
        put("surfaceAlpha", surfaceAlpha.toDouble())
        put("shadowElevation", shadowElevation.value.toDouble())
        put("shadowColor", shadowColor.toArgb())
        put("highlightColor", highlightColor.toArgb())
        put("glowRadius", glowRadius.value.toDouble())
        put("glowColor", glowColor.toArgb())
        put("fontScale", fontScale.toDouble())
        put("useSystemFont", useSystemFont)
        put("isDark", isDark)
    }

    private fun JSONObject.toThemeParameters(default: ThemeParameters): ThemeParameters {
        return ThemeParameters(
            primaryColor = Color(optInt("primaryColor", default.primaryColor.toArgb())),
            secondaryColor = Color(optInt("secondaryColor", default.secondaryColor.toArgb())),
            tertiaryColor = Color(optInt("tertiaryColor", default.tertiaryColor.toArgb())),
            backgroundColor = Color(optInt("backgroundColor", default.backgroundColor.toArgb())),
            surfaceColor = Color(optInt("surfaceColor", default.surfaceColor.toArgb())),
            surfaceVariantColor = Color(optInt("surfaceVariantColor", default.surfaceVariantColor.toArgb())),
            onPrimaryColor = Color(optInt("onPrimaryColor", default.onPrimaryColor.toArgb())),
            onSurfaceColor = Color(optInt("onSurfaceColor", default.onSurfaceColor.toArgb())),
            onBackgroundColor = Color(optInt("onBackgroundColor", default.onBackgroundColor.toArgb())),
            cornerRadiusScale = optDouble("cornerRadiusScale", default.cornerRadiusScale.toDouble()).toFloat(),
            borderStrokeWidth = optDouble("borderStrokeWidth", default.borderStrokeWidth.value.toDouble()).toFloat().dp,
            borderColor = Color(optInt("borderColor", default.borderColor.toArgb())),
            surfaceBlur = optDouble("surfaceBlur", default.surfaceBlur.value.toDouble()).toFloat().dp,
            surfaceAlpha = optDouble("surfaceAlpha", default.surfaceAlpha.toDouble()).toFloat(),
            shadowElevation = optDouble("shadowElevation", default.shadowElevation.value.toDouble()).toFloat().dp,
            shadowColor = Color(optInt("shadowColor", default.shadowColor.toArgb())),
            highlightColor = Color(optInt("highlightColor", default.highlightColor.toArgb())),
            glowRadius = optDouble("glowRadius", default.glowRadius.value.toDouble()).toFloat().dp,
            glowColor = Color(optInt("glowColor", default.glowColor.toArgb())),
            fontScale = optDouble("fontScale", default.fontScale.toDouble()).toFloat(),
            useSystemFont = optBoolean("useSystemFont", default.useSystemFont),
            isDark = optBoolean("isDark", default.isDark)
        )
    }
}
