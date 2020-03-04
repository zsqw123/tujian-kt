package io.nichijou.tujian.common.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.work.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.nichijou.tujian.common.R
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.facebook.imagepipeline.common.Priority
import com.facebook.imagepipeline.common.RotationOptions
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Category
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.entity.getNewUrl
import io.nichijou.tujian.common.notification.NotificationController
import jp.wasabeef.fresco.processors.BlurPostprocessor
import jp.wasabeef.fresco.processors.CombinePostProcessors
import jp.wasabeef.fresco.processors.gpu.PixelationFilterPostprocessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class WallpaperWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams), KoinComponent {

  private val tujianService by inject<TujianService>()
  private val tujianStore by inject<TujianStore>()

  override val coroutineContext: CoroutineDispatcher
    get() = IO

  override suspend fun doWork(): Result {
    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!wallpaperManager.isWallpaperSupported) {
        applicationContext.toast(R.string.not_supported_wallpaper)
        WallpaperConfig.enable = false
        stopLoad()
        return Result.failure()
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        if (!wallpaperManager.isSetWallpaperAllowed) {
          applicationContext.toast(R.string.not_allowed_wallpaper)
          WallpaperConfig.enable = false
          stopLoad()
          return Result.failure()
        }
      }
    }
    picture()
    return Result.success()
  }

  private suspend fun picture(pid: String) {
    val response = tujianService.member(pid)
    if (response.isSuccessful) {
      val picture = response.body()
      if (picture == null) {
        applicationContext.toast(R.string.picture_not_found)
      } else {
        picture.from = Picture.FROM_WALLPAPER
        tujianStore.insertPicture(picture)
        setWallpaper(picture)
      }
    }
  }

  private suspend fun picture() = getRandomPicture()

  private suspend fun getRandomPicture() {
    val response = tujianService.random()
    val picture = response.body()?.get(0)
    if (response.isSuccessful && picture != null) {
      picture.from = Picture.FROM_WALLPAPER
      tujianStore.insertPicture(picture)
      setWallpaper(picture)
    }
  }

  private suspend fun setWallpaper(picture: Picture) = withContext(Main) {
    val url: String = getNewUrl(picture) ?: return@withContext
    Glide.with(applicationContext).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
      override fun onLoadCleared(placeholder: Drawable?) {}
      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        NotificationController.notifyWallpaperUpdated(applicationContext, picture)
        doAsync {
          WallpaperManager.getInstance(applicationContext).setBitmap(resource)
        }
        val pid = inputData.getString("pid")
        if (!pid.isNullOrBlank()) {
          stopLoad()
        }
      }
    })
  }

  companion object {
    private const val WALLPAPER_WORKER = "io.nichijou.tujian.WALLPAPER_WORKER"
    private const val PID = "pid"
    @JvmStatic
    fun enqueueLoad(pid: String? = null) {
      val constraintsBuilder = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(WallpaperConfig.requiresBatteryNotLow)
        .setRequiresCharging(WallpaperConfig.requiresCharging)
        .setRequiresStorageNotLow(WallpaperConfig.requiresStorageNotLow)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        constraintsBuilder.setRequiresDeviceIdle(WallpaperConfig.requiresDeviceIdle)
      }
      val request = PeriodicWorkRequest.Builder(WallpaperWorker::class.java, WallpaperConfig.interval, TimeUnit.MILLISECONDS)
        .setConstraints(constraintsBuilder.build())
        .setInputData(Data.Builder().putString(PID, pid).build())
        .build()
      WorkManager.getInstance().enqueueUniquePeriodicWork(WALLPAPER_WORKER, ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    @JvmStatic
    fun stopLoad() {
      WorkManager.getInstance().cancelUniqueWork(WALLPAPER_WORKER)
    }
  }
}
