package io.nichijou.tujian.common.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.imagepipeline.request.ImageRequest
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.appwidget.*
import io.nichijou.tujian.common.entity.Bing
import io.nichijou.tujian.common.entity.Hitokoto
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.entity.getNewUrl
import io.nichijou.tujian.common.ext.ContentUriImageLoader
import io.nichijou.tujian.common.ext.toURI
import io.nichijou.tujian.common.fresco.getFileFromDiskCache
import io.nichijou.tujian.common.wallpaper.WallpaperConfig
import io.nichijou.tujian.common.wallpaper.WallpaperWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast

class NotificationController : BroadcastReceiver() {

  companion object {
    private const val NOTIFICATION_GROUP_WALLPAPER = "NOTIFICATION_GROUP_WALLPAPER"
    private const val NOTIFICATION_CHANNEL_WALLPAPER = "NOTIFICATION_CHANNEL_WALLPAPER"
    private const val NOTIFICATION_ID_WALLPAPER = Int.MAX_VALUE - 1

    private const val NOTIFICATION_GROUP_HITOKOTO = "NOTIFICATION_GROUP_HITOKOTO"
    private const val NOTIFICATION_CHANNEL_HITOKOTO = "NOTIFICATION_CHANNEL_HITOKOTO"
    private const val NOTIFICATION_ID_HITOKOTO = Int.MAX_VALUE - 2

    private const val NOTIFICATION_GROUP_BING = "NOTIFICATION_GROUP_BING"
    private const val NOTIFICATION_CHANNEL_BING = "NOTIFICATION_CHANNEL_BING"

    private const val NOTIFICATION_GROUP_TUJIAN = "NOTIFICATION_GROUP_TUJIAN"
    private const val NOTIFICATION_CHANNEL_TUJIAN = "NOTIFICATION_CHANNEL_TUJIAN"
    private const val NOTIFICATION_ID_TUJIAN = Int.MAX_VALUE - 3

    private const val ACTION_WALLPAPER_COPY = "io.nichijou.tujian.notification.action.ACTION_WALLPAPER_COPY"
    private const val ACTION_WALLPAPER_NEXT = "io.nichijou.tujian.notification.action.ACTION_WALLPAPER_NEXT"
    private const val ACTION_WALLPAPER_DOWNLOAD = "io.nichijou.tujian.notification.action.ACTION_WALLPAPER_DOWNLOAD"
    private const val ACTION_TUJIAN_DOWNLOAD = "io.nichijou.tujian.notification.action.ACTION_TUJIAN_DOWNLOAD"
    private const val ACTION_TUJIAN_NEXT = "io.nichijou.tujian.notification.action.ACTION_TUJIAN_NEXT"
    private const val ACTION_TUJIAN_COPY = "io.nichijou.tujian.notification.action.ACTION_TUJIAN_COPY"
    private const val ACTION_TUJIAN_COPY_HITOKOTO = "io.nichijou.tujian.notification.action.ACTION_TUJIAN_COPY_HITOKOTO"
    private const val ACTION_BING_NEXT = "io.nichijou.tujian.notification.action.ACTION_BING_NEXT"
    private const val ACTION_BING_COPY = "io.nichijou.tujian.notification.action.ACTION_BING_COPY"
    private const val ACTION_BING_DOWNLOAD = "io.nichijou.tujian.notification.action.ACTION_BING_DOWNLOAD"
    private const val ACTION_HITOKOTO_NEXT = "io.nichijou.tujian.notification.action.ACTION_HITOKOTO_NEXT"
    private const val ACTION_HITOKOTO_COPY = "io.nichijou.tujian.notification.action.ACTION_HITOKOTO_COPY"

    fun notifyWallpaperUpdated(context: Context, picture: Picture) = GlobalScope.launch {
      if (!WallpaperConfig.notification) return@launch
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?
        ?: return@launch
      createWallpaperNotificationChannel(context, notificationManager)
      val largeIconHeight = context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)

      var glideBitmap: Bitmap?
      Glide.with(context).asBitmap().load(getNewUrl(picture) + "!w480").into(object : CustomTarget<Bitmap>() {
        override fun onLoadCleared(placeholder: Drawable?) {}
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          glideBitmap = resource
          val title = picture.title + " via " + picture.user
          val style = NotificationCompat.BigPictureStyle()
            .bigLargeIcon(null)
            .setBigContentTitle(title)
            .setSummaryText(picture.desc)
            .bigPicture(glideBitmap)
          val notifyBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_WALLPAPER)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setGroup(NOTIFICATION_GROUP_WALLPAPER)
            .setTicker(context.getString(R.string.wallpaper_updated))
            .setSmallIcon(R.drawable.ic_tujian)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setLargeIcon(ThumbnailUtils.extractThumbnail(glideBitmap, largeIconHeight, largeIconHeight))
            .setContentTitle(title)
            .setContentText(picture.desc)
            .setStyle(style)
          val nextPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, NotificationController::class.java).setAction(ACTION_WALLPAPER_NEXT), PendingIntent.FLAG_UPDATE_CURRENT)
          val nextAction = NotificationCompat.Action.Builder(R.drawable.ic_round_navigate_next, context.getString(R.string.next), nextPendingIntent).build()
          notifyBuilder.addAction(nextAction)
          val copyIntent = Intent(context, NotificationController::class.java).setAction(ACTION_WALLPAPER_COPY).putExtra("picture", picture)
          val copyPendingIntent = PendingIntent.getBroadcast(context, 0, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
          val copyAction = NotificationCompat.Action.Builder(R.drawable.ic_round_file_copy, context.getString(R.string.copy), copyPendingIntent).build()
          notifyBuilder.addAction(copyAction)
          val downloadIntent = Intent(context, NotificationController::class.java).setAction(ACTION_WALLPAPER_DOWNLOAD).putExtra("picture", picture)
          val downloadPendingIntent = PendingIntent.getBroadcast(context, 0, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT)
          val downloadAction = NotificationCompat.Action.Builder(R.drawable.ic_round_save_alt, context.getString(R.string.download), downloadPendingIntent).build()
          notifyBuilder.addAction(downloadAction)
          val summaryNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_WALLPAPER)
            .setContentTitle(context.getString(R.string.wallpaper_updated))
            .setContentText(context.getString(R.string.following_switched_wallpaper))
            .setSmallIcon(R.drawable.ic_tujian)
            .setStyle(NotificationCompat.InboxStyle()
              .setBigContentTitle(context.getString(R.string.following_switched_wallpaper))
              .setSummaryText(context.getString(R.string.already_switched_wallpaper)))
            .setGroup(NOTIFICATION_GROUP_WALLPAPER)
            .setGroupSummary(true)
            .build()
          notificationManager.notify(("${picture.pid}<=>$NOTIFICATION_CHANNEL_WALLPAPER").hashCode(), notifyBuilder.build())
          notificationManager.notify(NOTIFICATION_ID_WALLPAPER, summaryNotification)
        }
      })

    }

    fun notifyBingAppWidgetUpdated(context: Context, bing: Bing) = GlobalScope.launch {
      if (!BingAppWidgetConfig.notification) return@launch
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?
        ?: return@launch
      createBingAppWidgetNotificationChannel(context, notificationManager)
      val pic = ImageRequest.fromUri(bing.url)?.getFileFromDiskCache() ?: return@launch
      if (!pic.exists()) return@launch
      val largeIconHeight = context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
      val uri = (pic.toURI(context) ?: return@launch)
      val imageLoader = ContentUriImageLoader(context.contentResolver, uri)
      val largeIcon = imageLoader.decode(largeIconHeight) ?: return@launch
      val bigPicture = imageLoader.decode(400) ?: return@launch
      val style = NotificationCompat.BigPictureStyle()
        .bigLargeIcon(null)
        .setBigContentTitle(bing.date)
        .setSummaryText(bing.copyright)
        .bigPicture(bigPicture)
      val notifyBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_BING)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setGroup(NOTIFICATION_GROUP_BING)
        .setTicker(context.getString(R.string.bing_appwidget_updated))
        .setSmallIcon(R.drawable.ic_tujian)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setLargeIcon(largeIcon)
        .setContentTitle(bing.date)
        .setContentText(bing.copyright)
        .setStyle(style)
      val nextPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, NotificationController::class.java).setAction(ACTION_BING_NEXT), PendingIntent.FLAG_UPDATE_CURRENT)
      val nextAction = NotificationCompat.Action.Builder(R.drawable.ic_round_navigate_next, context.getString(R.string.next), nextPendingIntent).build()
      notifyBuilder.addAction(nextAction)
      val copyIntent = Intent(context, NotificationController::class.java).setAction(ACTION_BING_COPY).putExtra("bing", bing)
      val copyPendingIntent = PendingIntent.getBroadcast(context, 0, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      val copyAction = NotificationCompat.Action.Builder(R.drawable.ic_round_file_copy, context.getString(R.string.copy), copyPendingIntent).build()
      notifyBuilder.addAction(copyAction)
      val downloadIntent = Intent(context, NotificationController::class.java).setAction(ACTION_BING_DOWNLOAD).putExtra("bing", bing)
      val downloadPendingIntent = PendingIntent.getBroadcast(context, 0, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      val downloadAction = NotificationCompat.Action.Builder(R.drawable.ic_round_save_alt, context.getString(R.string.download), downloadPendingIntent).build()
      notifyBuilder.addAction(downloadAction)
      notificationManager.notify(bing.date.hashCode(), notifyBuilder.build())
    }

    fun notifyHitokotoAppWidgetUpdated(context: Context, hitokoto: Hitokoto) = GlobalScope.launch {
      if (!HitokotoAppWidgetConfig.notification) return@launch
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?
        ?: return@launch
      createHitokotoAppWidgetNotificationChannel(context, notificationManager)
      val style = NotificationCompat.BigTextStyle()
        .bigText(hitokoto.hitokoto)
        .setBigContentTitle(hitokoto.source)
      val notifyBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_HITOKOTO)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setGroup(NOTIFICATION_GROUP_HITOKOTO)
        .setTicker(context.getString(R.string.hitokoto_appwidget_updated))
        .setSmallIcon(R.drawable.ic_tujian)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setContentTitle(hitokoto.source)
        .setContentText(hitokoto.hitokoto)
        .setStyle(style)
      val nextPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, NotificationController::class.java).setAction(ACTION_HITOKOTO_NEXT), PendingIntent.FLAG_UPDATE_CURRENT)
      val nextAction = NotificationCompat.Action.Builder(R.drawable.ic_round_navigate_next, context.getString(R.string.next), nextPendingIntent).build()
      notifyBuilder.addAction(nextAction)
      val copyIntent = Intent(context, NotificationController::class.java).setAction(ACTION_HITOKOTO_COPY).putExtra("hitokoto", hitokoto)
      val copyPendingIntent = PendingIntent.getBroadcast(context, 0, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      val copyAction = NotificationCompat.Action.Builder(R.drawable.ic_round_file_copy, context.getString(R.string.copy_hitokoto), copyPendingIntent).build()
      notifyBuilder.addAction(copyAction)
      val summaryNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_HITOKOTO)
        .setContentTitle(context.getString(R.string.hitokoto_appwidget_updated))
        .setContentText(context.getString(R.string.following_switched_hitokoto_appwidget))
        .setSmallIcon(R.drawable.ic_tujian)
        .setStyle(NotificationCompat.InboxStyle()
          .setBigContentTitle(context.getString(R.string.following_switched_hitokoto_appwidget))
          .setSummaryText(context.getString(R.string.already_switched_hitokoto_appwidget)))
        .setGroup(NOTIFICATION_GROUP_HITOKOTO)
        .setGroupSummary(true)
        .build()
      notificationManager.notify(("${hitokoto.hitokoto}<=>${hitokoto.source}").hashCode(), notifyBuilder.build())
      notificationManager.notify(NOTIFICATION_ID_HITOKOTO, summaryNotification)
    }

    fun notifyTujianAppWidgetUpdated(context: Context, picture: Picture) = GlobalScope.launch {
      if (!TujianAppWidgetConfig.notification) return@launch
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?
        ?: return@launch
      createTujianAppWidgetNotificationChannel(context, notificationManager)
      val largeIconHeight = context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)

      var glideBitmap: Bitmap?
      Glide.with(context).asBitmap().load(getNewUrl(picture) + "!w480").into(object : CustomTarget<Bitmap>() {
        override fun onLoadCleared(placeholder: Drawable?) {}
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          glideBitmap = resource

          val title = picture.title + " via " + picture.user
          val style = NotificationCompat.BigPictureStyle()
            .bigLargeIcon(null)
            .setBigContentTitle(title)
            .setSummaryText(picture.desc)
            .bigPicture(glideBitmap)
          val notifyBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_TUJIAN)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setGroup(NOTIFICATION_GROUP_TUJIAN)
            .setTicker(context.getString(R.string.tujian_appwidget_updated))
            .setSmallIcon(R.drawable.ic_tujian)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setLargeIcon(ThumbnailUtils.extractThumbnail(glideBitmap, largeIconHeight, largeIconHeight))
            .setContentTitle(title)
            .setContentText(picture.desc)
            .setStyle(style)
          val nextPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, NotificationController::class.java).setAction(ACTION_TUJIAN_NEXT), PendingIntent.FLAG_UPDATE_CURRENT)
          val nextAction = NotificationCompat.Action.Builder(R.drawable.ic_round_navigate_next, context.getString(R.string.next), nextPendingIntent).build()
          notifyBuilder.addAction(nextAction)
          val copyIntent = Intent(context, NotificationController::class.java).setAction(ACTION_TUJIAN_COPY).putExtra("picture", picture)
          val copyPendingIntent = PendingIntent.getBroadcast(context, 0, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
          val copyAction = NotificationCompat.Action.Builder(R.drawable.ic_round_file_copy, context.getString(R.string.copy), copyPendingIntent).build()
          notifyBuilder.addAction(copyAction)
          val downloadIntent = Intent(context, NotificationController::class.java).setAction(ACTION_TUJIAN_DOWNLOAD).putExtra("picture", picture)
          val downloadPendingIntent = PendingIntent.getBroadcast(context, 0, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT)
          val downloadAction = NotificationCompat.Action.Builder(R.drawable.ic_round_save_alt, context.getString(R.string.download), downloadPendingIntent).build()
          notifyBuilder.addAction(downloadAction)
          val summaryNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_TUJIAN)
            .setContentTitle(context.getString(R.string.tujian_appwidget_updated))
            .setContentText(context.getString(R.string.following_switched_tujian_appwidget))
            .setSmallIcon(R.drawable.ic_tujian)
            .setStyle(NotificationCompat.InboxStyle()
              .setBigContentTitle(context.getString(R.string.following_switched_tujian_appwidget))
              .setSummaryText(context.getString(R.string.already_switched_tujian_appwidget)))
            .setGroup(NOTIFICATION_GROUP_TUJIAN)
            .setGroupSummary(true)
            .build()
          notificationManager.notify(("${picture.pid}<=>$NOTIFICATION_CHANNEL_TUJIAN").hashCode(), notifyBuilder.build())
          notificationManager.notify(NOTIFICATION_ID_TUJIAN, summaryNotification)
        }
      })

    }

    private fun createWallpaperNotificationChannel(context: Context, notificationManager: NotificationManager) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_WALLPAPER) == null) {
        val newChannel = NotificationChannel(
          NOTIFICATION_CHANNEL_WALLPAPER,
          context.getString(R.string.notification_wallpaper_channel_name),
          NotificationManager.IMPORTANCE_MIN)
        newChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(newChannel)
      }
    }

    private fun createTujianAppWidgetNotificationChannel(context: Context, notificationManager: NotificationManager) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_TUJIAN) == null) {
        val newChannel = NotificationChannel(
          NOTIFICATION_CHANNEL_TUJIAN,
          context.getString(R.string.notification_tujian_channel_name),
          NotificationManager.IMPORTANCE_MIN)
        newChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(newChannel)
      }
    }

    private fun createBingAppWidgetNotificationChannel(context: Context, notificationManager: NotificationManager) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_BING) == null) {
        val newChannel = NotificationChannel(
          NOTIFICATION_CHANNEL_BING,
          context.getString(R.string.notification_bing_channel_name),
          NotificationManager.IMPORTANCE_MIN)
        newChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(newChannel)
      }
    }

    private fun createHitokotoAppWidgetNotificationChannel(context: Context, notificationManager: NotificationManager) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_HITOKOTO) == null) {
        val newChannel = NotificationChannel(
          NOTIFICATION_CHANNEL_HITOKOTO,
          context.getString(R.string.notification_hitokoto_channel_name),
          NotificationManager.IMPORTANCE_MIN)
        newChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(newChannel)
      }
    }
  }

  override fun onReceive(context: Context, intent: Intent?) {
    GlobalScope.launch {
      when (intent?.action) {
        ACTION_WALLPAPER_NEXT -> WallpaperWorker.enqueueLoad()
        ACTION_TUJIAN_NEXT -> TujianAppWidgetWorker.enqueueLoad()
        ACTION_BING_NEXT -> BingAppWidgetWorker.enqueueLoad()
        ACTION_HITOKOTO_NEXT -> HitokotoAppWidgetWorker.enqueueLoad()
        ACTION_WALLPAPER_COPY, ACTION_TUJIAN_COPY -> {
          val picture = intent.getParcelableExtra<Picture>("picture")
          if (picture == null) {
            context.toast(R.string.no_picture_info_available)
          } else {
            picture.copy(context)
          }
        }
        ACTION_BING_COPY -> {
          val bing = intent.getParcelableExtra<Bing>("bing")
          if (bing == null) {
            context.toast(R.string.no_picture_info_available)
          } else {
            bing.copy(context)
          }
        }
        ACTION_WALLPAPER_DOWNLOAD, ACTION_TUJIAN_DOWNLOAD -> {
          val picture = intent.getParcelableExtra<Picture>("picture")
          if (picture == null) {
            context.toast(R.string.no_picture_info_available)
          } else {
            picture.download(context)
          }
        }
        ACTION_BING_DOWNLOAD -> {
          val bing = intent.getParcelableExtra<Bing>("bing")
          if (bing == null) {
            context.toast(R.string.no_picture_info_available)
          } else {
            bing.download(context)
          }
        }
        ACTION_TUJIAN_COPY_HITOKOTO, ACTION_HITOKOTO_COPY -> {
          val hitokoto = intent.getParcelableExtra<Hitokoto>("hitokoto")
          if (hitokoto == null) {
            context.toast(R.string.no_hitokoto_available)
          } else {
            hitokoto.copy(context)
          }
        }
      }
    }
  }
}
