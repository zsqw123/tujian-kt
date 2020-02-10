package io.nichijou.tujian.func.wallpaper

import android.graphics.drawable.*
import android.os.*
import android.service.quicksettings.*
import androidx.annotation.*
import androidx.lifecycle.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.func.R
import io.nichijou.tujian.func.ServiceLifecycleDispatcher
import io.nichijou.tujian.func.shortcuts.*

@RequiresApi(Build.VERSION_CODES.N)
class WallpaperNextTileService : TileService(), LifecycleOwner {

  private var isEnable = false

  override fun onCreate() {
    lifecycleDispatcher.onServiceLifecycleEventCreate()
    WallpaperConfig.asLiveData(WallpaperConfig::enable).observe(this, Observer {
      isEnable = it
      ShortcutsController.updateShortcuts(applicationContext)
      if (isEnable) {
        qsTile?.apply {
          state = Tile.STATE_ACTIVE
          icon = Icon.createWithResource(applicationContext, R.drawable.ic_round_navigate_next)
          label = getString(R.string.next_wallpaper)
          updateTile()
        }
      } else {
        qsTile?.apply {
          state = Tile.STATE_INACTIVE
          icon = Icon.createWithResource(applicationContext, R.drawable.ic_tujian)
          label = getString(R.string.enable_wallpaper_switch)
          updateTile()
        }
        WallpaperWorker.stopLoad()// 停止定时worker
      }
    })
  }

  override fun onClick() {
    if (isEnable) {
      WallpaperWorker.enqueueLoad()
    } else {
      WallpaperConfig.enable = true
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
