package io.nichijou.tujian.ui.bing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Bing
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class BingViewModel(private val tujianService: TujianService, private val tujianStore: TujianStore) : ViewModel() {
  private lateinit var bings: LiveData<PagedList<Bing>>

  fun getBing(): LiveData<PagedList<Bing>> {
    if (!::bings.isInitialized) {
      bings = tujianStore.bingAsPaging()
    }
    viewModelScope.launch(IO) {
      val response = tujianService.bing()
      val data = response.body()?.data
      if (response.isSuccessful && !data.isNullOrEmpty()) {
        tujianStore.insertBing(data)
      }
    }
    return bings
  }
}
