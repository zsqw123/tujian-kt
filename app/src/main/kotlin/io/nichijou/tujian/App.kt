package io.nichijou.tujian

import android.app.*
import android.content.*
import android.graphics.*
import android.os.*
import com.chibatching.kotpref.*
import com.crashlytics.android.*
import com.facebook.cache.disk.*
import com.facebook.common.disk.*
import com.facebook.common.internal.*
import com.facebook.common.memory.*
import com.facebook.common.util.*
import com.facebook.drawee.backends.pipeline.*
import com.facebook.imagepipeline.cache.*
import com.facebook.imagepipeline.core.*
import com.facebook.imagepipeline.decoder.*
import com.facebook.imagepipeline.image.*
import com.facebook.stetho.*
import io.fabric.sdk.android.*
import io.nichijou.oops.*
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.fresco.*
import io.nichijou.tujian.func.shortcuts.*
import okhttp3.*
import org.koin.android.ext.android.*
import org.koin.android.ext.koin.*
import org.koin.core.context.*
import java.io.*

class App : Application() {
  private val okHttpClient: OkHttpClient by inject()

  override fun onCreate() {
    super.onCreate()
    GetContext.init(this)
    Stetho.initializeWithDefaults(this)
    Kotpref.init(this)
    startKoin {
      if (BuildConfig.DEBUG) {
        printLogger()
      }
      androidContext(this@App)
      modules(normalModule, commonModule)
    }
    initFresco()
    ShortcutsController.updateShortcuts(this)
    Oops.init(this)
  }

  override fun onLowMemory() {
    super.onLowMemory()
    Fresco.getImagePipeline().clearMemoryCaches()
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
      Fresco.getImagePipeline().clearMemoryCaches()
    }
  }

  private fun initFresco() {
    val memoryRegistry = NoOpMemoryTrimmableRegistry.getInstance()
    memoryRegistry.registerMemoryTrimmable {
      val suggestedTrimRatio = it.suggestedTrimRatio
      if (MemoryTrimType.OnCloseToDalvikHeapLimit.suggestedTrimRatio == suggestedTrimRatio
        || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.suggestedTrimRatio == suggestedTrimRatio
        || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.suggestedTrimRatio == suggestedTrimRatio
      ) {
        Fresco.getImagePipeline().clearMemoryCaches()
      }
    }
    val diskCacheConfig = DiskCacheConfig.newBuilder(this)
      .setBaseDirectoryPath(getDiskCacheDir(this))
      .setBaseDirectoryName(FRESCO_BASE_CACHE_DIR)
      .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
      .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)
      .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERY_LOW_SIZE)
      .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
      .build()
    val diskSmallCacheConfig = DiskCacheConfig.newBuilder(this)
      .setBaseDirectoryPath(getDiskCacheDir(this))
      .setBaseDirectoryName(FRESCO_SMALL_IMAGE_CACHE_DIR)
      .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
      .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)
      .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERY_LOW_SIZE)
      .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
      .build()
    val bitmapMemoryCacheParamsSupplier = Supplier<MemoryCacheParams> {
      MemoryCacheParams(
        getMaxCacheSize(),
        128,
        getMaxCacheSize(),
        Integer.MAX_VALUE,
        Integer.MAX_VALUE
      )
    }
    val progressiveJpegConfig = object : ProgressiveJpegConfig {
      override fun getNextScanNumberToDecode(scanNumber: Int): Int {
        return scanNumber + 2
      }

      override fun getQualityInfo(scanNumber: Int): QualityInfo {
        return ImmutableQualityInfo.of(scanNumber, scanNumber >= 5, false)
      }
    }
    val imagePipelineConfig = ImagePipelineConfig
      .newBuilder(this)
      .setDownsampleEnabled(true)
      .setBitmapsConfig(Bitmap.Config.ARGB_8888)
      .setResizeAndRotateEnabledForNetwork(true)
      .setMainDiskCacheConfig(diskCacheConfig)
      .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
      .setBitmapMemoryCacheParamsSupplier(bitmapMemoryCacheParamsSupplier)
      .setMemoryTrimmableRegistry(memoryRegistry)
      .setProgressiveJpegConfig(progressiveJpegConfig)
      .setNetworkFetcher(OkHttpNetworkFetcher(okHttpClient))
      .build()
    Fresco.initialize(this, imagePipelineConfig)
  }

  private fun getMaxCacheSize(): Int {
    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val maxMemory = Math.min(activityManager.memoryClass * ByteConstants.MB, Integer.MAX_VALUE)
    return when {
      maxMemory < 32 * ByteConstants.MB -> 4 * ByteConstants.MB
      maxMemory < 64 * ByteConstants.MB -> 8 * ByteConstants.MB
      else -> maxMemory / 8
    }
  }

  private fun getDiskCacheDir(context: Context): File {
    return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
      context.externalCacheDir!!
    } else {
      context.cacheDir
    }
  }

  companion object {
    private const val FRESCO_BASE_CACHE_DIR = "fresco_main_cache"
    private const val FRESCO_SMALL_IMAGE_CACHE_DIR = "fresco_small_image_cache"
    private const val MAX_DISK_CACHE_SIZE = Long.MAX_VALUE
    private const val MAX_DISK_CACHE_LOW_SIZE = (300 * ByteConstants.MB).toLong()
    private const val MAX_DISK_CACHE_VERY_LOW_SIZE = (100 * ByteConstants.MB).toLong()
  }
}
