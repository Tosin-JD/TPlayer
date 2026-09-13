package com.tosin.musicplayer.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class PlayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds, this::class.java.name)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId), this::class.java.name)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateDispatcher.updateAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            WidgetConstants.ACTION_WIDGET_UPDATE -> {
                WidgetUpdateDispatcher.updateAll(context)
            }
        }
    }

    companion object {
        fun updateWidgets(
            context: Context,
            manager: AppWidgetManager,
            widgetIds: IntArray,
            providerClassName: String? = null
        ) {
            val appContext = context.applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                val repo = WidgetStateRepository(appContext)
                val state = repo.loadState()
                val artworkBytes = repo.loadArtworkBytes()

                widgetIds.forEach { widgetId ->
                    val options = manager.getAppWidgetOptions(widgetId)
                    val views = WidgetRemoteViewsFactory.buildResponsive(appContext, state, artworkBytes, options, providerClassName)
                    manager.updateAppWidget(widgetId, views)
                }
            }
        }
    }
}