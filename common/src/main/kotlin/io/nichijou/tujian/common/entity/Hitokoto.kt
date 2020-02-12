package io.nichijou.tujian.common.entity

import android.content.Context
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.nichijou.tujian.common.ext.toClipboard
import kotlinx.android.parcel.Parcelize
import org.jetbrains.anko.toast

@Entity(tableName = "tb_hitokoto", indices = [Index(value = ["hitokoto", "source"], unique = true)])
@JsonClass(generateAdapter = true)
@Parcelize
data class Hitokoto(
  @PrimaryKey(autoGenerate = true)
  @Json(name = "ignore_id")// 错误的字段名用来忽略值
  val id: Int = 0,
  @Json(name = "hitokoto")
  val hitokoto: String, // 问题不是死不死心，一旦喜欢上了，就觉得非那个人莫属了。
  @Json(name = "source")
  val source: String // 人渣的本愿
) : BaseEntity(), Parcelable {
  fun share() = hitokoto + "\n" + source
  fun copy(context: Context) {
    context.toClipboard(share())
    context.toast(hitokoto)
  }
}
