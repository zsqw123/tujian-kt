package io.nichijou.tujian.ui.archive

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import io.nichijou.tujian.paging.LoadState

data class Listing<T>(
  val pagedList: LiveData<PagedList<T>>,
  val loadState: LiveData<LoadState>,
  val refreshState: LiveData<LoadState>,
  val refresh: () -> Unit,
  val retry: () -> Unit
)
