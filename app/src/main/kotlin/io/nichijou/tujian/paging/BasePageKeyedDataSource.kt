package io.nichijou.tujian.paging

import androidx.lifecycle.*
import androidx.paging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

abstract class BasePageKeyedDataSource<Key, Value>(private val scope: CoroutineScope) : PageKeyedDataSource<Key, Value>() {

  private var retry: (() -> Any)? = null

  val loadState = MutableLiveData<LoadState>()

  val initialLoad = MutableLiveData<LoadState>()

  fun retryAllFailed() {
    val prevRetry = retry
    retry = null
    prevRetry?.let {
      scope.launch(IO) {
        it.invoke()
      }
    }
  }

  override fun loadBefore(params: LoadParams<Key>, callback: LoadCallback<Key, Value>) = Unit

  protected fun retryLoadAfterOnFailure(params: LoadParams<Key>, callback: LoadCallback<Key, Value>, msg: String?) {
    retry = {
      loadAfter(params, callback)
    }
    loadState.postValue(LoadState.error(msg ?: "unknown err"))
  }

  protected fun retryLoadInitialOnFailure(params: LoadInitialParams<Key>, callback: LoadInitialCallback<Key, Value>, msg: String?) {
    retry = {
      loadInitial(params, callback)
    }
    val error = LoadState.error(msg ?: "unknown error")
    loadState.postValue(error)
    initialLoad.postValue(error)
  }

  protected fun initialLoading() {
    loadState.postValue(LoadState.LOADING)
    initialLoad.postValue(LoadState.LOADING)
  }

  protected fun initialLoaded() {
    retry = null
    loadState.postValue(LoadState.LOADED)
    initialLoad.postValue(LoadState.LOADED)
  }

  protected fun afterLoading() {
    loadState.postValue(LoadState.LOADING)
  }

  protected fun afterLoaded() {
    retry = null
    loadState.postValue(LoadState.LOADED)
  }

  protected fun finished() {
    retry = null
    loadState.postValue(LoadState.FINISHED)
  }
}
