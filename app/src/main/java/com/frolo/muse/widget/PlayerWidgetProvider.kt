package com.frolo.muse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.service.PlayerService


class PlayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val binder = peekService(context, Intent(context, PlayerService::class.java))
        val player = (binder as? PlayerService.PlayerBinder)?.service
        appWidgetManager.updatePlayerWidget(context, player, appWidgetId)
    }

    companion object {
        private val TAG = PlayerWidgetProvider::class.java.simpleName

        fun update(context: Context, player: Player?) {
            val widget = ComponentName(context, PlayerWidgetProvider::class.java)
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(widget)
            if (widgetIds != null && widgetIds.isNotEmpty()) {
                widgetManager.updatePlayerWidget(context, player, *widgetIds)
            }
        }
    }
}