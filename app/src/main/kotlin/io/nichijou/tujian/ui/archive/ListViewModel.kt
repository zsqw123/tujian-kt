package io.nichijou.tujian.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Picture
import java.util.concurrent.Executors

class ListViewModel(private val service: TujianService, private val dbStore: TujianStore) : ViewModel() {

  private val ioExecutor by lazy(LazyThreadSafetyMode.NONE) { Executors.newSingleThreadExecutor() }

  private val listRepository by lazy(LazyThreadSafetyMode.NONE) { ListRepository(ioExecutor, service, dbStore, viewModelScope) }

  fun get(tid: String): Listing<Picture> {
    return listRepository.get(tid, 10)
  }
}
