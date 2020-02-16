package io.nichijou.tujian.ui.settings

import android.provider.BaseColumns
import androidx.lifecycle.lifecycleScope
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.func.muzei.ArtworkWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
