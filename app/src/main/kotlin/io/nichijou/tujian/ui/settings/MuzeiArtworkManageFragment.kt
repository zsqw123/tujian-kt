package io.nichijou.tujian.ui.settings

import android.provider.*
import androidx.lifecycle.*
import com.google.android.apps.muzei.api.provider.*
import io.nichijou.tujian.R
import io.nichijou.tujian.base.*
import io.nichijou.tujian.ext.*
import io.nichijou.tujian.func.muzei.*
import kotlinx.coroutines.*

class MuzeiArtworkManageFragment : BaseFragment() {

  override fun getFragmentViewId(): Int = R.layout.fragment_settings_muzei

  override fun handleOnViewCreated() {
  }

  private fun queryArtwork() {
    lifecycleScope.launch(Dispatchers.IO) {
      val contentUri = ProviderContract.getContentUri(ArtworkWorker.getMuzeiAuthority(target()))
      val arts = mutableListOf<Artwork>()
      target().applicationContext.contentResolver.query(
        contentUri,
        null, null, null,
        BaseColumns._ID + " DESC")?.use {
        while (it.moveToNext()) {
          arts.add(Artwork.fromCursor(it))
        }
      }
      withContext(Dispatchers.Main) {

      }
    }
  }
}
