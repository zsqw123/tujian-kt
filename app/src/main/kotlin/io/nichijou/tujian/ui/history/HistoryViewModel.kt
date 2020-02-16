package io.nichijou.tujian.ui.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.BaseEntity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(private val tujianStore: TujianStore) : ViewModel() {
  private lateinit var history: MutableLiveData<List<BaseEntity>>

  fun getHistory(): MutableLiveData<List<BaseEntity>> {
    if (!::history.isInitialized) {
      history = MutableLiveData()
    }
    viewModelScope.launch {
      val list = tujianStore.history()
      withContext(Main) {
        history.value = list
      }
    }
    return history
  }
}
