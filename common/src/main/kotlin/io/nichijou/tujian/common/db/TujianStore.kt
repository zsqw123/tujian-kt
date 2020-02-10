package io.nichijou.tujian.common.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import io.nichijou.tujian.common.entity.Bing
import io.nichijou.tujian.common.entity.Category
import io.nichijou.tujian.common.entity.Hitokoto
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.logd

class TujianStore(private val context: Context) {

  fun categoriesAsLiveData(): LiveData<List<Category>> {
    return TuJianDatabase.getInstance(context).tujianDao().categoriesAsLiveData()
  }

  suspend fun categoriesAsync(): List<Category> {
    return TuJianDatabase.getInstance(context).tujianDao().categoriesAsync()
  }

  suspend fun insertCategory(category: Category) {
    TuJianDatabase.getInstance(context).tujianDao().insertCategory(category)
  }

  suspend fun insertCategory(categories: List<Category>) {
    TuJianDatabase.getInstance(context).tujianDao().insertCategory(categories)
  }

  fun lastPicture(): LiveData<Picture> = TuJianDatabase.getInstance(context).tujianDao().lastPictureAsLiveData()

  suspend fun insertPicture(pictures: List<Picture>) = TuJianDatabase.getInstance(context).tujianDao().insertPicture(pictures)

  suspend fun insertPicture(picture: Picture) = TuJianDatabase.getInstance(context).tujianDao().insertPicture(picture)

  fun picturesAsPaging(from: Int): LiveData<PagedList<Picture>> {
    val config = PagedList.Config.Builder()
      .setEnablePlaceholders(true)
      .setPageSize(10)
      .setInitialLoadSizeHint(20)
      .build()
    return LivePagedListBuilder(TuJianDatabase.getInstance(context).tujianDao().picturesAsPaging(from), config)
      .setBoundaryCallback(object : PagedList.BoundaryCallback<Picture>() {
        override fun onZeroItemsLoaded() {
          logd("onZeroItemsLoaded")
        }

        override fun onItemAtEndLoaded(itemAtEnd: Picture) {
          logd("onItemAtEndLoaded")
        }

        override fun onItemAtFrontLoaded(itemAtFront: Picture) {
          logd("onItemAtFrontLoaded")
        }
      })
      .build()
  }

  fun picturesAsPaging(tid: String, from: Int): LiveData<PagedList<Picture>> {
    val config = PagedList.Config.Builder()
      .setEnablePlaceholders(true)
      .setPageSize(10)
      .setInitialLoadSizeHint(20)
      .build()
    return LivePagedListBuilder(TuJianDatabase.getInstance(context).tujianDao().picturesAsPaging(tid, from), config).build()
  }

  fun picturesByAdded(): LiveData<PagedList<Picture>> {
    val config = PagedList.Config.Builder()
      .setEnablePlaceholders(true)
      .setPageSize(10)
      .setInitialLoadSizeHint(20)
      .build()
    return LivePagedListBuilder(TuJianDatabase.getInstance(context).tujianDao().picturesAsPagingByUpdated(), config).build()
  }

  fun picturesByAdded(tid: String): LiveData<PagedList<Picture>> {
    val config = PagedList.Config.Builder()
      .setEnablePlaceholders(true)
      .setPageSize(10)
      .setInitialLoadSizeHint(20)
      .build()
    return LivePagedListBuilder(TuJianDatabase.getInstance(context).tujianDao().picturesAsPagingByUpdated(tid), config).build()
  }

  fun hitokotoAsPaging(): LiveData<PagedList<Hitokoto>> {
    val config = PagedList.Config.Builder()
      .setEnablePlaceholders(true)
      .setPageSize(20)
      .setInitialLoadSizeHint(40)
      .build()
    return LivePagedListBuilder(TuJianDatabase.getInstance(context).tujianDao().hitokotoAsPaging(), config).build()
  }

  suspend fun insertHitokoto(hitokoto: Hitokoto) {
    TuJianDatabase.getInstance(context).tujianDao().insertHitokoto(hitokoto)
  }

  suspend fun lastHitokoto() = TuJianDatabase.getInstance(context).tujianDao().lastHitokoto()

  fun lastHitokotoAsLiveData() = TuJianDatabase.getInstance(context).tujianDao().lastHitokotoAsLiveData()

  fun bingAsPaging(): LiveData<PagedList<Bing>> {
    val config = PagedList.Config.Builder()
      .setEnablePlaceholders(true)
      .setPageSize(20)
      .setInitialLoadSizeHint(40)
      .build()
    return LivePagedListBuilder(TuJianDatabase.getInstance(context).tujianDao().bingAsPaging(), config).build()
  }

  suspend fun insertBing(bing: List<Bing>) = TuJianDatabase.getInstance(context).tujianDao().insertBing(bing)

  suspend fun insertBing(bing: Bing) = TuJianDatabase.getInstance(context).tujianDao().insertBing(bing)

  suspend fun history() = TuJianDatabase.getInstance(context).tujianDao().history()
}
