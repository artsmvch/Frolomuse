package com.frolo.muse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.service.PlayerService
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logPlayerWidgetDeleted
import com.frolo.muse.logger.logPlayerWidgetDisabled
import com.frolo.muse.logger.logPlayerWidgetEnabled


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

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        getEventLogger(context)?.logPlayerWidgetEnabled()
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds?.size?.also { count ->
            if (count > 0) {
                getEventLogger(context)?.logPlayerWidgetDeleted(count = count)
            }
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        getEventLogger(context)?.logPlayerWidgetDisabled()
    }

    private fun getEventLogger(context: Context): EventLogger? {
        val app = context.applicationContext as? FrolomuseApp
        return app?.appComponent?.provideEventLogger()
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