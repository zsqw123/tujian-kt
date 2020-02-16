package io.nichijou.tujian.ui.archive

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Picture
import kotlinx.coroutines.CoroutineScope

class ListDataSourceFactory(private val tid: String, private val service: TujianService, private val dbStore: TujianStore, private val scope: CoroutineScope) : DataSource.Factory<Int, Picture>() {
  val sourceLiveData = MutableLiveData<ListDataSource>()
  override fun create(): DataSource<Int, Picture> {
    val source = ListDataSource(tid, service, dbStore, scope)
    sourceLiveData.postValue(source)
    return source
  }
}
