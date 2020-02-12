package io.nichijou.tujian.func.muzei

import android.content.*
import android.net.*
import com.google.android.apps.muzei.api.*
import com.google.android.apps.muzei.api.provider.*
import com.google.android.apps.muzei.api.provider.Artwork
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.func.BuildConfig
import io.nichijou.tujian.func.R
import org.jetbrains.anko.toast


class ArtworkProvider : MuzeiArtProvider() {
  override fun getCommands(artwork: Artwork): MutableList<UserCommand> {
    return context?.let {
      mutableListOf(UserCommand(COMMAND_ID_OPEN_TUJIAN, it.getString(R.string.open_tujian)))
    } ?: super.getCommands(artwork)
  }

  override fun onCommand(artwork: Artwork, id: Int) {
    val context = context ?: return
    if (id == COMMAND_ID_OPEN_TUJIAN) {
      try {
        val uri = Uri.parse(BuildConfig.LAUNCH_TUJIAN_URL)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
      } catch (e: Exception) {
        logd("e: $e")
        context.toast(R.string.launch_tujian_error)
      }
    }
  }

  override fun onLoadRequested(initial: Boolean) {
    ArtworkWorker.enqueueLoad()
  }

  companion object {
    private const val COMMAND_ID_OPEN_TUJIAN = 1
  }
}
