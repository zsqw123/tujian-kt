package io.nichijou.tujian.func.muzei

import android.content.*
import android.net.*
import android.os.*
import androidx.work.*
import com.google.android.apps.muzei.api.provider.*
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.db.*
import io.nichijou.tujian.common.entity.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.func.BuildConfig
import io.nichijou.tujian.func.R
import kotlinx.coroutines.*
import org.koin.core.*
import kotlin.random.*


class ArtworkWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams), KoinComponent {

  private val tujianService by inject<TujianService>()
  private val tujianStore by inject<TujianStore>()

  override val coroutineContext: CoroutineDispatcher
    get() = Dispatchers.IO

  override suspend fun doWork(): Result {
    var categories: List<Category>? = tujianStore.categoriesAsync()
    if (categories.isNullOrEmpty()) {
      categories = getCategories()
    }
    val cid = MuzeiConfig.categoryId
    if (cid.isBlank()) {
      getRandomPicture()
    } else {
      val find = categories?.find { c -> c.tid == cid }
      if (find == null) {
        applicationContext.toast(R.string.muzei_can_not_match_category_id)
        getRandomPicture()
      } else {
        getCategoryPicture(find)
      }
    }
    return Result.success()
  }

  private suspend fun getCategoryPicture(find: Category) {
    val response = tujianService.list(find.tid, Random.nextInt(30) + 1, 1)
    val body = response.body()
    if (response.isSuccessful && body != null) {
      val picture = body.data[0]
      picture.from = Picture.FROM_MUZEI
      tujianStore.insertPicture(picture)
      addPicture2Muzei(picture)
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
      picture.from = Picture.FROM_MUZEI
      tujianStore.insertPicture(picture)
      addPicture2Muzei(picture)
    }
  }

  private fun addPicture2Muzei(picture: Picture) {
    ProviderContract.getProviderClient(applicationContext, getMuzeiAuthority(applicationContext))
      .addArtwork(
        Artwork().apply {
          token = picture.pid
          title = picture.title
          persistentUri = Uri.parse(picture.local)
          webUri = Uri.parse(picture.link)
          byline = "${picture.date} via ${picture.user}"
        }
      )
  }

  companion object {
    @JvmStatic
    fun getMuzeiAuthority(context: Context): String {
      return "${BuildConfig.ART_WORK_AUTHORITY}.${context.packageName}"
    }

    @JvmStatic
    fun enqueueLoad() {
      val constraintsBuilder = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(MuzeiConfig.requiresBatteryNotLow)
        .setRequiresCharging(MuzeiConfig.requiresCharging)
        .setRequiresStorageNotLow(MuzeiConfig.requiresStorageNotLow)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        constraintsBuilder.setRequiresDeviceIdle(MuzeiConfig.requiresDeviceIdle)
      }
      val request = OneTimeWorkRequestBuilder<ArtworkWorker>()
        .setConstraints(constraintsBuilder.build())
        .build()
      WorkManager.getInstance().enqueue(request)
    }
  }
}

