package io.nichijou.tujian.func.appwidget

import android.content.Context
import android.os.Build
import androidx.work.*
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Category
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.func.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import org.jetbrains.anko.toast
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TujianAppWidgetWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams), KoinComponent {

  private val tujianService by inject<TujianService>()
  private val tujianStore by inject<TujianStore>()

  override val coroutineContext: CoroutineDispatcher
    get() = IO

  override suspend fun doWork(): Result {
    if (!TujianAppWidgetProvider.hasAppWidgetEnabled(applicationContext)) {
      applicationContext.toast(R.string.enable_tujian_appwidget_to_home_screen)
      TujianAppWidgetConfig.enable = false
      stopLoad()
      return Result.success()
    }
    var categories: List<Category>? = tujianStore.categoriesAsync()
    if (categories.isNullOrEmpty()) {
      categories = getCategories()
    }
    val hiRep = tujianService.hitokoto()
    val hitokoto = hiRep.body()
    if (hiRep.isSuccessful && hitokoto != null) {
      tujianStore.insertHitokoto(hitokoto)
    }
    val cid = TujianAppWidgetConfig.categoryId
    if (cid.isBlank()) {
      getRandomPicture()
    } else {
      val find = categories?.find { c -> c.tid == cid }
      if (find == null) {
        applicationContext.toast(R.string.appwidget_can_not_match_category_id)
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
      picture.from = Picture.FROM_APPWIDGET
      tujianStore.insertPicture(picture)
      TujianAppWidgetProvider.updateWidgetsNew(applicationContext)
    }
  }

  private suspend fun getRandomPicture() {
    val response = tujianService.random()
    val picture = response.body()
    if (response.isSuccessful && picture != null) {
      picture.from = Picture.FROM_APPWIDGET
      tujianStore.insertPicture(picture)
      TujianAppWidgetProvider.updateWidgetsNew(applicationContext)
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

  companion object {
    private const val APPWIDGET_WORKER = "io.nichijou.tujian.appwidget.tujian.WORKER"

    @JvmStatic
    fun enqueueLoad() {
      val constraintsBuilder = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(TujianAppWidgetConfig.requiresBatteryNotLow)
        .setRequiresCharging(TujianAppWidgetConfig.requiresCharging)
        .setRequiresStorageNotLow(TujianAppWidgetConfig.requiresStorageNotLow)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        constraintsBuilder.setRequiresDeviceIdle(TujianAppWidgetConfig.requiresDeviceIdle)
      }
      val request = PeriodicWorkRequest.Builder(TujianAppWidgetWorker::class.java, TujianAppWidgetConfig.interval, TimeUnit.MILLISECONDS)
        .setConstraints(constraintsBuilder.build())
        .build()
      WorkManager.getInstance().enqueueUniquePeriodicWork(APPWIDGET_WORKER, ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    @JvmStatic
    fun stopLoad() {
      WorkManager.getInstance().cancelUniqueWork(APPWIDGET_WORKER)
    }
  }
}
