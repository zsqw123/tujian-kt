package io.nichijou.tujian.ui.archive

import androidx.lifecycle.*
import androidx.paging.*
import io.nichijou.tujian.paging.*

data class Listing<T>(
  val pagedList: LiveData<PagedList<T>>,
  val loadState: LiveData<LoadState>,
  val refreshState: LiveData<LoadState>,
  val refresh: () -> Unit,
  val retry: () -> Unit
)
