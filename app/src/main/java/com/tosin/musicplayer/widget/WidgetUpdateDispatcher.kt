package com.tosin.musicplayer.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdateDispatcher {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            val manager = AppWidgetManager.getInstance(appContext)
            val providers = listOf(
                PlayerWidgetProvider::class.java,
                CompactWidgetProvider::class.java,
                FullscreenWidgetProvider::class.java
            )

            providers.forEach { providerClass ->
                val widgetIds = manager.getAppWidgetIds(ComponentName(appContext, providerClass))
                if (widgetIds.isNotEmpty()) {
                    PlayerWidgetProvider.updateWidgets(appContext, manager, widgetIds, providerClass.name)
                }
            }
        }
    }
}
