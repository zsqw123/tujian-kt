package io.nichijou.tujian.func.appwidget


import android.content.*
import android.os.*
import androidx.work.*
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.db.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.func.R
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.koin.core.*
import java.util.concurrent.*

class BingAppWidgetWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams), KoinComponent {

  private val tujianService by inject<TujianService>()
  private val tujianStore by inject<TujianStore>()

  override val coroutineContext: CoroutineDispatcher
    get() = IO

  override suspend fun doWork(): Result {
    if (!BingAppWidgetProvider.hasAppWidgetEnabled(applicationContext)) {
      applicationContext.toast(R.string.enable_bing_appwidget_to_home_screen)
      BingAppWidgetConfig.enable = false
      stopLoad()
      return Result.success()
    }
    val response = tujianService.bing()
    val body = response.body()
    if (response.isSuccessful && body != null && !body.data.isNullOrEmpty()) {
      tujianStore.insertBing(body.data[0])
      BingAppWidgetProvider.updateWidgetsNew(applicationContext)
    }
    return Result.success()
  }

  companion object {
    private const val APPWIDGET_WORKER = "io.nichijou.tujian.appwidget.bing.WORKER"

    @JvmStatic
    fun enqueueLoad() {
      val constraintsBuilder = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(BingAppWidgetConfig.requiresBatteryNotLow)
        .setRequiresCharging(BingAppWidgetConfig.requiresCharging)
        .setRequiresStorageNotLow(BingAppWidgetConfig.requiresStorageNotLow)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        constraintsBuilder.setRequiresDeviceIdle(BingAppWidgetConfig.requiresDeviceIdle)
      }
      val request = PeriodicWorkRequest.Builder(BingAppWidgetWorker::class.java, 12, TimeUnit.HOURS)
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
