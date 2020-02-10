package io.nichijou.tujian.common.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.nichijou.tujian.common.entity.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@Dao
abstract class TujianDao {
  @Query("select * from tb_category")
  abstract fun categoriesAsLiveData(): LiveData<List<Category>>

  @Query("select * from tb_category")
  abstract suspend fun categoriesAsync(): List<Category>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertCategory(category: Category)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertCategory(categories: List<Category>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertPicture(picture: Picture)

  @Query("select * from tb_picture order by updated desc limit 1")
  abstract fun lastPictureAsLiveData(): LiveData<Picture>

  @Query("select * from tb_picture where `from` = :from order by updated desc limit 1")
  abstract fun lastPicture(from: Int): Picture?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertPicture(pictures: List<Picture>)

  @Query("select * from tb_picture order by date desc")
  abstract fun picturesAsPaging(): DataSource.Factory<Int, Picture>

  @Query("select * from tb_picture order by updated desc")
  abstract suspend fun picturesAsync(): List<Picture>

  @Query("select * from tb_picture where `from` = :from order by date desc")
  abstract fun picturesAsPaging(from: Int): DataSource.Factory<Int, Picture>

  @Query("select * from tb_picture where tid = :tid and `from` = :from order by date desc")
  abstract fun picturesAsPaging(tid: String, from: Int): DataSource.Factory<Int, Picture>

  @Query("select * from tb_picture order by updated desc")
  abstract fun picturesAsPagingByUpdated(): DataSource.Factory<Int, Picture>

  @Query("select * from tb_picture where tid = :tid order by updated desc")
  abstract fun picturesAsPagingByUpdated(tid: String): DataSource.Factory<Int, Picture>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertHitokoto(hitokoto: Hitokoto)

  @Query("select * from tb_hitokoto order by updated desc")
  abstract fun hitokotoAsPaging(): DataSource.Factory<Int, Hitokoto>

  @Query("select * from tb_hitokoto order by updated desc")
  abstract suspend fun hitokotoAsync(): List<Hitokoto>

  @Query("select * from tb_hitokoto order by updated desc limit 1")
  abstract suspend fun lastHitokoto(): Hitokoto?

  @Query("select * from tb_hitokoto order by updated desc limit 1")
  abstract fun lastHitokotoAsLiveData(): LiveData<List<Hitokoto>>

  @Query("select * from tb_bing order by date desc")
  abstract fun bingAsPaging(): DataSource.Factory<Int, Bing>

  @Query("select * from tb_bing order by updated desc")
  abstract suspend fun bingAsync(): List<Bing>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertBing(bing: List<Bing>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertBing(bing: Bing)

  @Query("select * from tb_bing order by date desc limit 1")
  abstract suspend fun lastBingAsync(): Bing?

  suspend fun history() = coroutineScope {
    val pictures = async {
      picturesAsync()
    }
    val bings = async {
      bingAsync()
    }
    val hitokotos = async {
      hitokotoAsync()
    }
    val list = (pictures.await() + bings.await() + hitokotos.await()) as List<BaseEntity>
    return@coroutineScope list.sortedWith(Comparator { a, b ->
      val offset = a.updated - b.updated
      return@Comparator when {
        offset > 0 -> -1
        offset < 0 -> 1
        else -> 0
      }
    })
  }
}

