package io.nichijou.tujian.ui.bing

import androidx.lifecycle.*
import androidx.paging.*
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.db.*
import io.nichijou.tujian.common.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

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
