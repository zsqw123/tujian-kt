package io.nichijou.tujian.common.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue.*
import android.widget.RemoteViews
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.facebook.imagepipeline.common.Priority
import com.facebook.imagepipeline.common.RotationOptions
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.db.TuJianDatabase
import io.nichijou.tujian.common.entity.Bing
import io.nichijou.tujian.common.ext.getWallpaperPrimaryColorCompat
import io.nichijou.tujian.common.ext.logd
import io.nichijou.tujian.common.ext.loge
import io.nichijou.tujian.common.ext.scale
import io.nichijou.tujian.common.notification.NotificationController
import io.nichijou.utils.bodyColor
import jp.wasabeef.fresco.processors.BlurPostprocessor
import jp.wasabeef.fresco.processors.CombinePostProcessors
import jp.wasabeef.fresco.processors.gpu.PixelationFilterPostprocessor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BingAppWidgetProvider : AppWidgetProvider() {

  override fun onEnabled(context: Context) {
    BingAppWidgetWorker.enqueueLoad()
    BingAppWidgetConfig.enable = true
  }

  override fun onDisabled(context: Context) {
    BingAppWidgetWorker.stopLoad()
    BingAppWidgetConfig.enable = false
  }

  override fun onReceive(context: Context, intent: Intent?) {
    super.onReceive(context, intent)
    when (intent?.action) {
      ACTION_NEXT -> BingAppWidgetWorker.enqueueLoad()
      ACTION_SAVE -> bing?.download(context)
      ACTION_COPY -> bing?.copy(context)
    }
  }

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    updateWidgets(context)
  }

  override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
    updateWidgets(context)
  }

  companion object {
    const val ACTION_NEXT = "io.nichijou.tujian.appwidget.action.bing.ACTION_NEXT"
    const val ACTION_SAVE = "io.nichijou.tujian.appwidget.action.bing.ACTION_SAVE"
    const val ACTION_COPY = "io.nichijou.tujian.appwidget.action.bing.ACTION_COPY"

    fun hasAppWidgetEnabled(context: Context) = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, BingAppWidgetProvider::class.java)).isNotEmpty()
    private var bing: Bing? = null

    private fun updateWidgets(context: Context) {
      GlobalScope.launch {
        bing?.apply {
          doUpdateWidget(context, this)
        }
      }
    }

    fun updateWidgetsNew(context: Context) {
      GlobalScope.launch {
        bing = TuJianDatabase.getInstance(context).tujianDao().lastBingAsync()?.apply {
          doUpdateWidget(context, this)
        }
      }
    }

    private fun doUpdateWidget(context: Context, bing: Bing) {
      val uri = Uri.parse(bing.url) ?: return
      val builder = ImageRequestBuilder.newBuilderWithSource(uri)
        .setRotationOptions(RotationOptions.autoRotate())
        .setRequestPriority(Priority.HIGH)
        .setImageDecodeOptions(ImageDecodeOptions.newBuilder().setBitmapConfig(Bitmap.Config.ARGB_8888).build())
      val blur = BingAppWidgetConfig.blur
      val pixel = BingAppWidgetConfig.pixel
      if (blur || pixel) {
        val processorBuilder = CombinePostProcessors.Builder()
        if (blur) processorBuilder.add(BlurPostprocessor(context, BingAppWidgetConfig.blurValue / 100))
        if (pixel) processorBuilder.add(PixelationFilterPostprocessor(context, (BingAppWidgetConfig.pixelValue / 100).toFloat()))
        builder.postprocessor = processorBuilder.build()
      }
      val imageRequest = builder.build()
      Fresco.getImagePipeline().fetchDecodedImage(imageRequest, null).subscribe(object : BaseBitmapDataSubscriber() {
        override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
        }

        override fun onNewResultImpl(bitmap: Bitmap?) {
          logd("AppWidget bitmap is null? : ${bitmap == null}")
          if (bitmap != null) {
            val widget = ComponentName(context, BingAppWidgetProvider::class.java)
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
              var success = false
              while (!success) {
                val nextIntent = Intent(context, BingAppWidgetProvider::class.java).apply {
                  action = ACTION_NEXT
                }
                val saveIntent = Intent(context, BingAppWidgetProvider::class.java).apply {
                  action = ACTION_SAVE
                }
                val copyIntent = Intent(context, BingAppWidgetProvider::class.java).apply {
                  action = ACTION_COPY
                }
                val nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                val savePendingIntent = PendingIntent.getBroadcast(context, 0, saveIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                val copyPendingIntent = PendingIntent.getBroadcast(context, 0, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                val scaledImage = bitmap.scale(widgetWidth, widgetHeight)
                val remoteViews = RemoteViews(context.packageName, R.layout.widget_bing)
                scaledImage?.run {
                  remoteViews.setImageViewBitmap(R.id.banner, scaledImage)
                }
                val textColor: Int = if (BingAppWidgetConfig.autoTextColor) {
                  context.getWallpaperPrimaryColorCompat().bodyColor()
                } else {
                  Color.WHITE
                }
                remoteViews.setOnClickPendingIntent(R.id.banner, nextPendingIntent)
                remoteViews.setOnClickPendingIntent(R.id.save, savePendingIntent)
                remoteViews.setOnClickPendingIntent(R.id.copyright, copyPendingIntent)
                remoteViews.setTextViewText(R.id.copyright, "\t\t\t\t${bing.copyright} - ${bing.date}")
                remoteViews.setTextColor(R.id.copyright, textColor)
                remoteViews.setTextViewTextSize(R.id.hitokoto, COMPLEX_UNIT_SP, (BingAppWidgetConfig.textSize / 100).toFloat())
                var lines = BingAppWidgetConfig.textLines
                when {
                  lines == 0 -> lines = -1
                  lines < 100 -> lines = 1
                  else -> lines /= 100
                }
                remoteViews.setInt(R.id.hitokoto, "setMaxLines", lines)
                try {
                  appWidgetManager.updateAppWidget(widgetId, remoteViews)
                  NotificationController.notifyBingAppWidgetUpdated(context, bing)
                  success = true
                } catch (e: IllegalArgumentException) {
                  loge("App widget size $widgetWidth x $widgetHeight exceeded maximum memory, reducing quality", e)
                  widgetWidth /= 2
                  widgetHeight /= 2
                }
              }
            }
          }
        }
      }, CallerThreadExecutor.getInstance())
    }
  }
}
