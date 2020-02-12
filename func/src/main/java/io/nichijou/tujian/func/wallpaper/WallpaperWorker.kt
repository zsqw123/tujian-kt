package io.nichijou.tujian.func.wallpaper


import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.work.*
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
import io.nichijou.tujian.func.R
import io.nichijou.tujian.func.notification.NotificationController
import jp.wasabeef.fresco.processors.BlurPostprocessor
import jp.wasabeef.fresco.processors.CombinePostProcessors
import jp.wasabeef.fresco.processors.gpu.PixelationFilterPostprocessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
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
    val pid = inputData.getString("pid")
    if (pid.isNullOrBlank()) {
      picture()
    } else {
      picture(pid)
    }
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

  private suspend fun picture() {
    var categories: List<Category>? = tujianStore.categoriesAsync()
    if (categories.isNullOrEmpty()) {
      categories = getCategories()
    }
    val cid = WallpaperConfig.categoryId
    if (cid.isBlank()) {
      getRandomPicture()
    } else {
      val find = categories?.find { c -> c.tid == cid }
      if (find == null) {
        applicationContext.toast(R.string.wallpaper_can_not_match_category_id)
        getRandomPicture()
      } else {
        getCategoryPicture(find)
      }
    }
  }

  private suspend fun getCategoryPicture(find: Category) {
    val response = tujianService.list(find.tid, Random.nextInt(30) + 1, 1)
    val body = response.body()
    if (response.isSuccessful && body != null) {
      val picture = body.data[0]
      picture.from = Picture.FROM_WALLPAPER
      tujianStore.insertPicture(picture)
      setWallpaper(picture)
    }
  }

  private suspend fun getCategories(): List<Category>? {
    val response = tujianService.category()
    val data = response.body()?.data
    return if (response.isSuccessful && !data.isNullOrEmpty()) {
      tujianStore.insertCategory(data)
      data
    } else {
      applicationContext.toast(R.string.get_tujian_category_error)
      null
    }
  }

  private suspend fun getRandomPicture() {
    val response = tujianService.random()
    val picture = response.body()
    if (response.isSuccessful && picture != null) {
      picture.from = Picture.FROM_WALLPAPER
      tujianStore.insertPicture(picture)
      setWallpaper(picture)
    }
  }

  private suspend fun setWallpaper(picture: Picture) = withContext(Main) {
    val uri = Uri.parse(picture.local) ?: return@withContext
    val builder = ImageRequestBuilder.newBuilderWithSource(uri)
      .setRotationOptions(RotationOptions.autoRotate())
      .setRequestPriority(Priority.HIGH)
      .setImageDecodeOptions(ImageDecodeOptions.newBuilder().setBitmapConfig(Bitmap.Config.ARGB_8888).build())
    val blur = WallpaperConfig.blur
    val pixel = WallpaperConfig.pixel
    if (blur || pixel) {
      val processorBuilder = CombinePostProcessors.Builder()
      if (blur) processorBuilder.add(BlurPostprocessor(applicationContext, WallpaperConfig.blurValue / 10))
      if (pixel) processorBuilder.add(PixelationFilterPostprocessor(applicationContext, (WallpaperConfig.pixelValue / 10).toFloat()))
      builder.postprocessor = processorBuilder.build()
    }
    val imageRequest = builder.build()
    val dataSource = Fresco.getImagePipeline().fetchDecodedImage(imageRequest, null)
    dataSource.subscribe(object : BaseBitmapDataSubscriber() {
      override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
        val pid = inputData.getString("pid")
        if (!pid.isNullOrBlank()) {
          stopLoad()
        }
      }

      override fun onNewResultImpl(bitmap: Bitmap?) {
        if (bitmap != null) {
          WallpaperManager.getInstance(applicationContext).setBitmap(bitmap)
          NotificationController.notifyWallpaperUpdated(applicationContext, picture)
        }
        val pid = inputData.getString("pid")
        if (!pid.isNullOrBlank()) {
          stopLoad()
        }
      }
    }, UiThreadImmediateExecutorService.getInstance())
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
