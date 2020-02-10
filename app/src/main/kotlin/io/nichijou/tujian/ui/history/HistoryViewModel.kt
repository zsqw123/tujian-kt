package io.nichijou.tujian.ui.history

import androidx.lifecycle.*
import io.nichijou.tujian.common.db.*
import io.nichijou.tujian.common.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

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
