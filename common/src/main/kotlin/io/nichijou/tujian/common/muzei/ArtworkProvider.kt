package io.nichijou.tujian.common.muzei

import android.content.Intent
import android.net.Uri
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import io.nichijou.tujian.common.BuildConfig
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.ext.logd
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
