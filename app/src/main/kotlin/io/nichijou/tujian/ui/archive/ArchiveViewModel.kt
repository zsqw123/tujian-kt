package io.nichijou.tujian.ui.archive

import androidx.lifecycle.*
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.db.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class ArchiveViewModel(private val tujianService: TujianService, private val tujianStore: TujianStore) : ViewModel() {
  val categories by lazy(LazyThreadSafetyMode.NONE) {
    viewModelScope.launch(IO) {
      val response = tujianService.category()
      val data = response.body()?.data
      if (response.isSuccessful && !data.isNullOrEmpty()) {
        tujianStore.insertCategory(data)
      }
    }
    tujianStore.categoriesAsLiveData()
  }
}
