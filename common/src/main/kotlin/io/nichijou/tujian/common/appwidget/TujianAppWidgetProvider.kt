package io.nichijou.tujian.common.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.db.TuJianDatabase
import io.nichijou.tujian.common.entity.Hitokoto
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.entity.getNewUrl
import io.nichijou.tujian.common.ext.loge
import io.nichijou.tujian.common.ext.scale
import io.nichijou.tujian.common.notification.NotificationController
import io.nichijou.tujian.common.shortcuts.ShortcutsController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TujianAppWidgetProvider : AppWidgetProvider() {

  override fun onEnabled(context: Context) {
    TujianAppWidgetWorker.enqueueLoad()
    TujianAppWidgetConfig.enable = true
    ShortcutsController.updateShortcuts(context)
  }

  override fun onDisabled(context: Context) {
    TujianAppWidgetWorker.stopLoad()
    TujianAppWidgetConfig.enable = false
    ShortcutsController.updateShortcuts(context)
  }

  override fun onReceive(context: Context, intent: Intent?) {
    super.onReceive(context, intent)
    when (intent?.action) {
      ACTION_NEXT -> TujianAppWidgetWorker.enqueueLoad()
      ACTION_SAVE -> picture?.download(context)
    }
  }

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    updateWidgets(context)
  }

  override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
    updateWidgets(context)
  }

  companion object {
    const val ACTION_NEXT = "io.nichijou.tujian.appwidget.action.tujian.ACTION_NEXT"
    const val ACTION_SAVE = "io.nichijou.tujian.appwidget.action.tujian.ACTION_SAVE"
    fun hasAppWidgetEnabled(context: Context) = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, TujianAppWidgetProvider::class.java)).isNotEmpty()
    private var hitokoto: Hitokoto? = null
    private var picture: Picture? = null

    private fun updateWidgets(context: Context) {
      GlobalScope.launch {
        doUpdateWidget(context)
      }
    }

    fun updateWidgetsNew(context: Context) {
      GlobalScope.launch {
        picture = TuJianDatabase.getInstance(context).tujianDao().lastPicture(Picture.FROM_APPWIDGET)
        doUpdateWidget(context)
      }
    }

    private fun doUpdateWidget(context: Context) {
      val widget = ComponentName(context, TujianAppWidgetProvider::class.java)
      val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
      val appWidgetIds = appWidgetManager.getAppWidgetIds(widget) ?: return
      if (appWidgetIds.isEmpty()) {
        return
      }
      val displayMetrics = context.resources.displayMetrics
      val minWidgetSize = context.resources.getDimensionPixelSize(R.dimen.widget_min_size)
      for (widgetId in appWidgetIds) {
        val extras = appWidgetManager.getAppWidgetOptions(widgetId)
        var widgetWidth = applyDimension(COMPLEX_UNIT_DIP, extras.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH).toFloat(), displayMetrics).toInt()
        widgetWidth = widgetWidth.coerceAtMost(displayMetrics.widthPixels).coerceAtLeast(minWidgetSize)
        var widgetHeight = applyDimension(COMPLEX_UNIT_DIP, extras.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).toFloat(), displayMetrics).toInt()
        widgetHeight = widgetHeight.coerceAtMost(displayMetrics.heightPixels).coerceAtLeast(minWidgetSize)
        var glideBitmap: Bitmap?
        Glide.with(context).asBitmap().load(getNewUrl(picture) + "!w720").into(object : CustomTarget<Bitmap>(widgetWidth, widgetHeight) {
          override fun onLoadCleared(placeholder: Drawable?) {}
          override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            glideBitmap = resource
            val remoteViews = updateWidgetPic(context, glideBitmap!!, widgetWidth, widgetHeight)
            try {
              appWidgetManager.updateAppWidget(widgetId, remoteViews)
              NotificationController.notifyTujianAppWidgetUpdated(context, picture!!)
            } catch (e: IllegalArgumentException) {
              loge("App widget size $widgetWidth x $widgetHeight exceeded maximum memory, reducing quality", e)
              widgetWidth /= 2
              widgetHeight /= 2
            }
          }
        })
      }
    }

    fun updateWidgetPic(context: Context, glideBitmap: Bitmap, widgetWidth: Int, widgetHeight: Int): RemoteViews {
      val nextIntent = Intent(context, TujianAppWidgetProvider::class.java).apply { action = ACTION_NEXT }
      val saveIntent = Intent(context, TujianAppWidgetProvider::class.java).apply { action = ACTION_SAVE }
      val nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      val savePendingIntent = PendingIntent.getBroadcast(context, 0, saveIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      val scaledImage = glideBitmap.scale(widgetWidth, widgetHeight)
      val remoteViews = RemoteViews(context.packageName, R.layout.widget_tujian)
      scaledImage?.run {
        remoteViews.setImageViewBitmap(R.id.banner, scaledImage)
        remoteViews.setViewVisibility(R.id.text_load, View.GONE)
      }
      remoteViews.setOnClickPendingIntent(R.id.banner, nextPendingIntent)
      remoteViews.setOnClickPendingIntent(R.id.save, savePendingIntent)
      return remoteViews

    }
  }
}
