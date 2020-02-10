package io.nichijou.tujian.common.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "tb_category")
@JsonClass(generateAdapter = true)
@Parcelize
data class Category(
  @PrimaryKey
  @Json(name = "TID")
  val tid: String, // e5771003-b4ed-11e8-a8ea-0202761b0892
  @Json(name = "level")
  val level: Int, // 1
  @Json(name = "T_NAME")
  val name: String,// 电脑壁纸
  @Json(name = "ignore_updated")// 错误的字段名用来忽略值
  val updated: Long = System.currentTimeMillis()
) : Parcelable
