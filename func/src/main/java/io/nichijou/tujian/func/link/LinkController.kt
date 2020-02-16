package io.nichijou.tujian.func.link

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.nichijou.tujian.func.R
import io.nichijou.tujian.func.wallpaper.WallpaperWorker
import org.jetbrains.anko.toast

class LinkController : AppCompatActivity() {
  init {
    lifecycleScope.launchWhenCreated {
      val uri = intent.data
      if (uri == null) {
        toast(R.string.parse_uri_error)
      } else {
        val func = uri.getQueryParameter("func")
        val pid = uri.getQueryParameter("pid")
        when {
          func.isNullOrBlank() -> {
            val it = Intent()
            it.component = ComponentName(this@LinkController.packageName, "io.nichijou.tujian.ui.MainActivity")
            startActivity(it)
          }
          func == FUNC_WALLPAPER -> {
            if (pid.isNullOrBlank() || pid.length != 36) {
              applicationContext.toast(R.string.wallpaper_param_error)
            } else {
              applicationContext.toast(R.string.wallpaper_loading)
              WallpaperWorker.enqueueLoad(pid)
            }
          }
        }
      }
    }
  }

  companion object {
    private const val FUNC_WALLPAPER = "wp"
  }
}
