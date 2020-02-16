package io.nichijou.tujian.common.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.nichijou.tujian.common.entity.Bing
import io.nichijou.tujian.common.entity.Category
import io.nichijou.tujian.common.entity.Hitokoto
import io.nichijou.tujian.common.entity.Picture

@Database(entities = [(Category::class), (Picture::class), (Hitokoto::class), (Bing::class)], version = 2, exportSchema = false)
abstract class TuJianDatabase : RoomDatabase() {
  abstract fun tujianDao(): TujianDao

  companion object {
    @Volatile
    private var instance: TuJianDatabase? = null

    fun getInstance(context: Context): TuJianDatabase {
      return instance ?: synchronized(this) {
        instance
          ?: Room.databaseBuilder(context, TuJianDatabase::class.java, "${context.packageName}.db")
            .fallbackToDestructiveMigration().build().also { instance = it }
      }
    }

    //不会升级 删库重建
//    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
//      override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE users "
//          + " ADD COLUMN last_update INTEGER")
//      }
//    }
  }
}
