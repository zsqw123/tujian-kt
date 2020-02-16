package io.nichijou.tujian.ui.archive

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Picture
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executor

class ListRepository(private val executor: Executor, private val service: TujianService, private val dbStore: TujianStore, private val scope: CoroutineScope) {
  @MainThread
  fun get(tid: String, pageSize: Int): Listing<Picture> {
    val sourceFactory = ListDataSourceFactory(tid, service, dbStore, scope)
    val livePagedList = sourceFactory.toLiveData(pageSize = pageSize, fetchExecutor = executor)
    val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
      it.initialLoad
    }
    return Listing(
      pagedList = livePagedList,
      loadState = Transformations.switchMap(sourceFactory.sourceLiveData) {
        it.loadState
      },
      retry = {
        sourceFactory.sourceLiveData.value?.retryAllFailed()
      },
      refresh = {
        sourceFactory.sourceLiveData.value?.invalidate()
      },
      refreshState = refreshState
    )
  }
}
