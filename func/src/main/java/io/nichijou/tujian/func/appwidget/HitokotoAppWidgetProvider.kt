package io.nichijou.tujian.func.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.widget.RemoteViews
import io.nichijou.tujian.common.db.TuJianDatabase
import io.nichijou.tujian.common.entity.Hitokoto
import io.nichijou.tujian.common.ext.getWallpaperPrimaryColorCompat
import io.nichijou.tujian.func.R
import io.nichijou.tujian.func.notification.NotificationController
import io.nichijou.utils.bodyColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HitokotoAppWidgetProvider : AppWidgetProvider() {

  override fun onEnabled(context: Context) {
    HitokotoAppWidgetWorker.enqueueLoad()
    HitokotoAppWidgetConfig.enable = true
  }

  override fun onDisabled(context: Context) {
    HitokotoAppWidgetWorker.stopLoad()
    HitokotoAppWidgetConfig.enable = false
  }

  override fun onReceive(context: Context, intent: Intent?) {
    super.onReceive(context, intent)
    if (intent?.action == ACTION_NEXT) {
      HitokotoAppWidgetWorker.enqueueLoad()
      hitokoto?.copy(context)
    }
  }

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    updateWidgets(context)
  }

  override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
    updateWidgets(context)
  }

  companion object {
    const val ACTION_NEXT = "io.nichijou.tujian.appwidget.action.hitokoto.ACTION_NEXT"

    fun hasAppWidgetEnabled(context: Context) = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, HitokotoAppWidgetProvider::class.java)).isNotEmpty()
    private var hitokoto: Hitokoto? = null

    private fun updateWidgets(context: Context) {
      GlobalScope.launch {
        hitokoto?.apply {
          doUpdateWidget(context, this)
        }
      }
    }

    fun updateWidgetsNew(context: Context) {
      GlobalScope.launch {
        hitokoto = TuJianDatabase.getInstance(context).tujianDao().lastHitokoto()?.apply {
          doUpdateWidget(context, this)
        }
      }
    }

    private fun doUpdateWidget(context: Context, hitokoto: Hitokoto) {
      val widget = ComponentName(context, HitokotoAppWidgetProvider::class.java)
      val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
      val appWidgetIds = appWidgetManager.getAppWidgetIds(widget) ?: return
      if (appWidgetIds.isEmpty()) {
        return
      }
      for (widgetId in appWidgetIds) {
        val nextIntent = Intent(context, HitokotoAppWidgetProvider::class.java).apply {
          action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_hitokoto)
        val textColor: Int = if (HitokotoAppWidgetConfig.autoTextColor) {
          context.getWallpaperPrimaryColorCompat().bodyColor()
        } else {
          Color.WHITE
        }
        remoteViews.setOnClickPendingIntent(R.id.wrapper, nextPendingIntent)
        remoteViews.setTextViewText(R.id.hitokoto, "\t\t\t\t${hitokoto.hitokoto}")
        remoteViews.setTextViewText(R.id.source, hitokoto.source)
        remoteViews.setTextColor(R.id.hitokoto, textColor)
        remoteViews.setTextColor(R.id.source, textColor)
        remoteViews.setTextViewTextSize(R.id.hitokoto, COMPLEX_UNIT_SP, (HitokotoAppWidgetConfig.hitokotoTextSize / 100).toFloat())
        remoteViews.setTextViewTextSize(R.id.source, COMPLEX_UNIT_SP, (HitokotoAppWidgetConfig.sourceTextSize / 100).toFloat())
        var lines = HitokotoAppWidgetConfig.hitokotoLines
        when {
          lines == 0 -> lines = -1
          lines < 100 -> lines = 1
          else -> lines /= 100
        }
        remoteViews.setInt(R.id.hitokoto, "setMaxLines", lines)
        try {
          appWidgetManager.updateAppWidget(widgetId, remoteViews)
          NotificationController.notifyHitokotoAppWidgetUpdated(context, hitokoto)
        } catch (e: IllegalArgumentException) {
        }
      }
    }
  }
}
