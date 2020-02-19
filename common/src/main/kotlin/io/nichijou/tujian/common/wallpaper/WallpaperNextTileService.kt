package io.nichijou.tujian.common.wallpaper

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.ServiceLifecycleDispatcher
import io.nichijou.tujian.common.ext.asLiveData
import io.nichijou.tujian.common.shortcuts.ShortcutsController

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
