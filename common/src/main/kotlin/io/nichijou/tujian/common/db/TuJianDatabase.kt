package io.nichijou.tujian.common.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.nichijou.tujian.common.entity.Bing
import io.nichijou.tujian.common.entity.Category
import io.nichijou.tujian.common.entity.Hitokoto
import io.nichijou.tujian.common.entity.Picture

@Database(entities = [(Category::class), (Picture::class), (Hitokoto::class), (Bing::class)], version = 1, exportSchema = false)
abstract class TuJianDatabase : RoomDatabase() {
  abstract fun tujianDao(): TujianDao

  companion object {
    @Volatile
    private var instance: TuJianDatabase? = null

    fun getInstance(context: Context): TuJianDatabase {
      return instance ?: synchronized(this) {
        instance
          ?: Room.databaseBuilder(context, TuJianDatabase::class.java, "${context.packageName}.db").build().also { instance = it }
      }
    }
  }
}
