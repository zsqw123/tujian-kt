package io.nichijou.tujian.common.appwidget

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.ext.asLiveData
import io.nichijou.tujian.common.ServiceLifecycleDispatcher
import org.jetbrains.anko.toast

@RequiresApi(Build.VERSION_CODES.N)
class TujianAppWidgetNextTileService : TileService(), LifecycleOwner {

  private var isEnable = false

  override fun onCreate() {
    lifecycleDispatcher.onServiceLifecycleEventCreate()
    TujianAppWidgetConfig.asLiveData(TujianAppWidgetConfig::enable).observe(this, Observer {
      isEnable = it
      if (isEnable) {
        qsTile?.apply {
          state = Tile.STATE_ACTIVE
          icon = Icon.createWithResource(applicationContext, R.drawable.ic_round_navigate_next)
          label = getString(R.string.next_appwidget)
          updateTile()
        }
      } else {
        qsTile?.apply {
          state = Tile.STATE_INACTIVE
          icon = Icon.createWithResource(applicationContext, R.drawable.ic_tujian)
          label = getString(R.string.enable_appwidget)
          updateTile()
        }
      }
    })
  }

  override fun onClick() {
    if (isEnable) {
      TujianAppWidgetWorker.enqueueLoad(context)
    } else {
      applicationContext.toast(R.string.enable_tujian_appwidget_to_home_screen)
    }
  }

  override fun onStartListening() {
    lifecycleDispatcher.onServiceLifecycleEventStart()
  }

  override fun onStopListening() {
    lifecycleDispatcher.onServiceLifecycleEventStop()
  }


  override fun onDestroy() {
    lifecycleDispatcher.onServiceLifecycleEventDestroy()
    super.onDestroy()
  }


  private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)

  override fun getLifecycle(): Lifecycle = lifecycleDispatcher.lifecycle
}
