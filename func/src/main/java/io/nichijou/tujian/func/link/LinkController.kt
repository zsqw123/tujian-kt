package io.nichijou.tujian.func.link

import android.content.*
import androidx.appcompat.app.*
import androidx.lifecycle.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.func.R
import io.nichijou.tujian.func.wallpaper.*


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
          func == FUNC_WALLAPER -> {
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
    private const val FUNC_WALLAPER = "wp"
  }
}
