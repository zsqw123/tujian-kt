package io.nichijou.tujian.common.appwidget

import android.content.Context
import android.os.Build
import androidx.work.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.nichijou.tujian.common.C
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Category
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.entity.SplashResp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.lang.Exception
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
    getRandomPicture()
    return Result.success()
  }

  private suspend fun getRandomPicture() {
    val response = tujianService.random()
    if (response.isSuccessful) {
      val picture = response.body()?.get(0)
      picture!!.from = Picture.FROM_APPWIDGET
      tujianStore.insertPicture(picture)
      TujianAppWidgetProvider.updateWidgetsNew(applicationContext)
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
