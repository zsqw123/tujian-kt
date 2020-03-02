package io.nichijou.tujian.common.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.appwidget.TujianAppWidgetConfig
import io.nichijou.tujian.common.appwidget.TujianAppWidgetWorker
import io.nichijou.tujian.common.wallpaper.WallpaperConfig
import io.nichijou.tujian.common.wallpaper.WallpaperWorker

@RequiresApi(Build.VERSION_CODES.N_MR1)
class ShortcutsController : FragmentActivity() {

  companion object {

    private const val SHORTCUTS_ACTION = "io.nichijou.tujian.shortcuts.action.SHORTCUTS"
    private const val EXTRA_ACTION = "SHORTCUTS_ACTION"
    private const val ID_WALLPAPER = "ID_WALLPAPER"
    private const val ID_APPWIDGET = "ID_APPWIDGET"

    private const val ACTION_ENABLE_WALLPAPER = "ACTION_ENABLE_WALLPAPER"
    private const val ACTION_NEXT_WALLPAPER = "ACTION_NEXT_WALLPAPER"
    private const val ACTION_NEXT_APPWIDGET = "ACTION_NEXT"

    fun updateShortcuts(context: Context) {
      if (haveShortcutManger(context)) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val wsc = createWallpaperShortcuts(context)
        val asc = createAppWidgetShortcuts(context)
        shortcutManager.addDynamicShortcuts(arrayListOf(wsc, asc))
        if (TujianAppWidgetConfig.enable) {
          shortcutManager.enableShortcuts(listOf(ID_WALLPAPER, ID_APPWIDGET))
        } else {
          shortcutManager.enableShortcuts(listOf(ID_WALLPAPER))
          shortcutManager.disableShortcuts(listOf(ID_APPWIDGET))
        }
      } else return
    }

    private fun createWallpaperShortcuts(context: Context): ShortcutInfo {
      return if (WallpaperConfig.enable) {
        ShortcutInfo.Builder(context, ID_WALLPAPER)
          .setIcon(Icon.createWithResource(context, R.drawable.ic_round_navigate_next))
          .setShortLabel(context.getString(R.string.next_wallpaper))
          .setIntent(createAction(context, ACTION_NEXT_WALLPAPER))
          .build()
      } else {
        ShortcutInfo.Builder(context, ID_WALLPAPER)
          .setIcon(Icon.createWithResource(context, R.drawable.ic_tujian))
          .setShortLabel(context.getString(R.string.enable_wallpaper_switch))
          .setIntent(createAction(context, ACTION_ENABLE_WALLPAPER))
          .build()
      }
    }

    private fun createAppWidgetShortcuts(context: Context): ShortcutInfo {
      return ShortcutInfo.Builder(context, ID_APPWIDGET)
        .setIcon(Icon.createWithResource(context, R.drawable.ic_round_navigate_next))
        .setShortLabel(context.getString(R.string.next_appwidget))
        .setDisabledMessage(context.getString(R.string.enable_tujian_appwidget_to_home_screen))
        .setIntent(createAction(context, ACTION_NEXT_APPWIDGET))
        .build()
    }

    private fun createAction(context: Context, action: String): Intent =
      Intent(context, ShortcutsController::class.java).apply {
        this.action = SHORTCUTS_ACTION
        putExtra(EXTRA_ACTION, action)
      }

    fun haveShortcutManger(context: Context): Boolean {
      return context.getSystemService(ShortcutManager::class.java) != null
    }
  }

  init {
    lifecycleScope.launchWhenCreated {
      when (intent?.getStringExtra(EXTRA_ACTION)) {
        ACTION_ENABLE_WALLPAPER -> {
          WallpaperConfig.enable = true
          WallpaperWorker.enqueueLoad()
          updateShortcuts(this@ShortcutsController)
        }
        ACTION_NEXT_WALLPAPER -> WallpaperWorker.enqueueLoad()
        ACTION_NEXT_APPWIDGET -> TujianAppWidgetWorker.enqueueLoad()
      }
      finish()
    }
  }
}
