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
        appWidgetManager.updatePlayerWidgets(context, peekPlayer(context), *appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        appWidgetManager.updatePlayerWidget(context, peekPlayer(context), appWidgetId, newOptions)
    }

    private fun peekPlayer(context: Context): Player? {
        val binder = peekService(context, Intent(context, PlayerService::class.java))
        return (binder as? PlayerService.PlayerBinder)?.service as? Player
    }

    companion object {
        private val TAG = PlayerWidgetProvider::class.java.simpleName

        fun update(context: Context, player: Player?) {
            val widget = ComponentName(context, PlayerWidgetProvider::class.java)
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(widget)
            if (widgetIds != null && widgetIds.isNotEmpty()) {
                widgetManager.updatePlayerWidgets(context, player, *widgetIds)
            }
        }
    }
}