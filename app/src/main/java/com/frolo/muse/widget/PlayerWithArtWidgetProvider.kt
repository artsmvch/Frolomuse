package com.frolo.muse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.service.PlayerService
import com.frolo.muse.engine.service.PlayerService.PlayerBinder


class PlayerWithArtWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val binder = peekService(context, Intent(context, PlayerService::class.java))
        val player = (binder as? PlayerBinder)?.service
        appWidgetManager.updatePlayerWidget(context, player, true, appWidgetId)
    }

    companion object {
        private val TAG = PlayerWithArtWidgetProvider::class.java.simpleName

        fun update(context: Context, player: Player?) {
            val widget = ComponentName(context, PlayerWithArtWidgetProvider::class.java)
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(widget)
            if (widgetIds != null && widgetIds.isNotEmpty()) {
                widgetManager.updatePlayerWidget(context, player, true, *widgetIds)
            }
        }
    }
}