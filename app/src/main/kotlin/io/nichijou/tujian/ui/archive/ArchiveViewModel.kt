package io.nichijou.tujian.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

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
