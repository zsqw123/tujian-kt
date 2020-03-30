package io.nichijou.tujian.common.appwidget

import android.content.Context
import android.os.Build
import androidx.work.*
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class BingAppWidgetWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams), KoinComponent {

  private val tujianService by inject<TujianService>()
  private val tujianStore by inject<TujianStore>()

  override suspend fun doWork(): Result {
    return withContext(IO){
      if (!BingAppWidgetProvider.hasAppWidgetEnabled(applicationContext)) {
        applicationContext.toast(R.string.enable_bing_appwidget_to_home_screen)
        BingAppWidgetConfig.enable = false
        stopLoad()
        return@withContext Result.success()
      }
      val response = tujianService.bing()
      val body = response.body()
      if (response.isSuccessful && body != null && !body.data.isNullOrEmpty()) {
        tujianStore.insertBing(body.data[0])
        BingAppWidgetProvider.updateWidgetsNew(applicationContext)
      }
      return@withContext Result.success()
    }

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
