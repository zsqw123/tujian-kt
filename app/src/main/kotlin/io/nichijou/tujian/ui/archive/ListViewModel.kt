package io.nichijou.tujian.ui.archive

import androidx.lifecycle.*
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.db.*
import io.nichijou.tujian.common.entity.*
import java.util.concurrent.*

class ListViewModel(private val service: TujianService, private val dbStore: TujianStore) : ViewModel() {

  private val ioExecutor by lazy(LazyThreadSafetyMode.NONE) { Executors.newSingleThreadExecutor() }

  private val listRepository by lazy(LazyThreadSafetyMode.NONE) { ListRepository(ioExecutor, service, dbStore, viewModelScope) }

  fun get(tid: String): Listing<Picture> {
    return listRepository.get(tid, 10)
  }
}
