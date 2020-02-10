package io.nichijou.tujian.ui.archive

import androidx.lifecycle.*
import androidx.paging.*
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.db.*
import io.nichijou.tujian.common.entity.*
import kotlinx.coroutines.*

class ListDataSourceFactory(private val tid: String, private val service: TujianService, private val dbStore: TujianStore, private val scope: CoroutineScope) : DataSource.Factory<Int, Picture>() {
  val sourceLiveData = MutableLiveData<ListDataSource>()
  override fun create(): DataSource<Int, Picture> {
    val source = ListDataSource(tid, service, dbStore, scope)
    sourceLiveData.postValue(source)
    return source
  }
}
