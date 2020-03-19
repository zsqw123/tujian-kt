package io.nichijou.tujian

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.chibatching.kotpref.Kotpref
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.common.disk.NoOpDiskTrimmableRegistry
import com.facebook.common.internal.Supplier
import com.facebook.common.memory.MemoryTrimType
import com.facebook.common.memory.NoOpMemoryTrimmableRegistry
import com.facebook.common.util.ByteConstants
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.cache.MemoryCacheParams
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig
import com.facebook.imagepipeline.image.ImmutableQualityInfo
import com.facebook.imagepipeline.image.QualityInfo
import io.nichijou.oops.Oops
import io.nichijou.tujian.common.commonModule
import io.nichijou.tujian.common.shortcuts.ShortcutsController
import me.jessyan.progressmanager.ProgressManager
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File
import java.io.InputStream

class App : Application() {

  var glideOkHttpClient: OkHttpClient? = null
  override fun onCreate() {
    super.onCreate()
    context = applicationContext
    Kotpref.init(applicationContext)
    glideOkHttpClient = ProgressManager.getInstance().with(OkHttpClient.Builder())
      .build()
    startKoin {
      if (BuildConfig.DEBUG) {
        printLogger()
      }
      androidContext(applicationContext)
      modules(normalModule, commonModule)
    }
    initFresco()
    Oops.init(this)
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        ShortcutsController.updateShortcuts(applicationContext)
    } catch (e: java.lang.Exception) {
      Log.e("no shortcut", e.message ?: "")
    }
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

  companion object {
    var context: Context? = null
    fun initFresco() {
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
      val diskCacheConfig = DiskCacheConfig.newBuilder(context)
        .setBaseDirectoryPath(getDiskCacheDir(context!!))
        .setBaseDirectoryName(FRESCO_BASE_CACHE_DIR)
        .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
        .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)
        .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERY_LOW_SIZE)
        .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
        .build()
      val diskSmallCacheConfig = DiskCacheConfig.newBuilder(context!!)
        .setBaseDirectoryPath(getDiskCacheDir(context!!))
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
        .newBuilder(context!!)
        .setDownsampleEnabled(true)
        .setBitmapsConfig(Bitmap.Config.ARGB_8888)
        .setResizeAndRotateEnabledForNetwork(true)
        .setMainDiskCacheConfig(diskCacheConfig)
        .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
        .setBitmapMemoryCacheParamsSupplier(bitmapMemoryCacheParamsSupplier)
        .setMemoryTrimmableRegistry(memoryRegistry)
        .setProgressiveJpegConfig(progressiveJpegConfig)
        .build()
//        .setNetworkFetcher(OkHttpNetworkFetcher(okHttpClient))

      Fresco.initialize(context, imagePipelineConfig)
    }

    private fun getMaxCacheSize(): Int {
      val activityManager = context!!.getSystemService(ACTIVITY_SERVICE) as ActivityManager
      val maxMemory = (activityManager.memoryClass * ByteConstants.MB).coerceAtMost(Integer.MAX_VALUE)
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


    private const val FRESCO_BASE_CACHE_DIR = "fresco_main_cache"
    private const val FRESCO_SMALL_IMAGE_CACHE_DIR = "fresco_small_image_cache"
    private const val MAX_DISK_CACHE_SIZE = Long.MAX_VALUE
    private const val MAX_DISK_CACHE_LOW_SIZE = (300 * ByteConstants.MB).toLong()
    private const val MAX_DISK_CACHE_VERY_LOW_SIZE = (100 * ByteConstants.MB).toLong()
  }
}

@GlideModule
class GlideConfiguration : AppGlideModule() {
  override fun applyOptions(context: Context, builder: GlideBuilder) {}
  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    val application = context.applicationContext as App
    //Glide 底层默认使用 HttpConnection 进行网络请求,这里替换为 Okhttp 后才能使用本框架,进行 Glide 的加载进度监听
    registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(application.glideOkHttpClient!!))
  }

  override fun isManifestParsingEnabled(): Boolean {
    return false
  }
}
