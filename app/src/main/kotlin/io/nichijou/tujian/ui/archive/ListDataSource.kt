package io.nichijou.tujian.ui.archive

import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.paging.BasePageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListDataSource(
  private val tid: String,
  private val service: TujianService,
  private val dbStore: TujianStore,
  private val scope: CoroutineScope
) : BasePageKeyedDataSource<Int, Picture>(scope) {

  override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Picture>) {
    scope.launch(Dispatchers.IO) {
      initialLoading()
      val response = service.list(tid, 1, params.requestedLoadSize / 2)
      val body = response.body()
      if (response.isSuccessful && body != null) {
        val data = body.data
        if (!data.isNullOrEmpty()) {
          dbStore.insertPicture(data)
          if (body.max > 1) {
            callback.onResult(data, null, 2)
          } else {
            callback.onResult(data, null, null)
            finished()
          }
        }
        initialLoaded()
      } else {
        retryLoadInitialOnFailure(params, callback, "load error: ${response.message()}")
      }
    }
  }

  override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Picture>) {
    scope.launch(Dispatchers.IO) {
      afterLoading()
      val response = service.list(tid, params.key, params.requestedLoadSize)
      val body = response.body()
      if (response.isSuccessful && body != null) {
        val data = body.data
        if (!data.isNullOrEmpty()) {
          if (params.key >= body.max) {
            callback.onResult(data, null)
            finished()
          } else {
            callback.onResult(data, params.key + 1)
          }
        }
        afterLoaded()
      } else {
        retryLoadAfterOnFailure(params, callback, "load error: ${response.message()}")
      }
    }
  }
}
