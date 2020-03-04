package io.nichijou.tujian.common.entity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.squareup.moshi.*
import io.nichijou.tujian.common.ext.saveToAlbum
import io.nichijou.tujian.common.ext.toClipboard
import kotlinx.android.parcel.Parcelize
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import java.util.*

@Entity(tableName = "tb_bing", indices = [Index(value = ["url", "date"], unique = true)])
@JsonClass(generateAdapter = true)
@Parcelize
data class Bing(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  @Json(name = "copyright")
  val copyright: String, // 澳大利亚太平洋海岸的海崖大桥 (© Nick Fox/Alamy)
  @Json(name = "enddate")
  val date: String, // 20190522
  @Json(name = "url")
  @BingUrl
  val url: String // /th?id=OHR.SeaCliffBridge_ZH-CN5362667487_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp, updated: Long
) : BaseEntity(), Parcelable {
  fun share() = "下载地址：$url\n版权信息：$copyright\n日期：$date"

  fun copy(context: Context) {
    context.toClipboard(share())
    context.runOnUiThread { context.toast(copyright) }
  }

  fun download(context: Context) {
    context.runOnUiThread { context.toast("开始下载原图...") }
    val name = "TujianBing-" + copyright + Date()
    Glide.with(context).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
      override fun onLoadCleared(placeholder: Drawable?) {}
      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        resource.saveToAlbum(context, name)
      }
    })
  }
}

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class BingUrl

class BingUrlAdapter {
  @ToJson
  fun toJson(@BingUrl url: String): String {
    return url
  }

  @FromJson
  @BingUrl
  fun fromJson(url: String): String {
    return "https://cn.bing.com$url"
  }
}
