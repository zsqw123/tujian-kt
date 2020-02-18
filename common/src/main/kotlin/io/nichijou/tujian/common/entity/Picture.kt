package io.nichijou.tujian.common.entity

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.afollestad.assent.Permission
import com.afollestad.assent.isAllGranted
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.nichijou.tujian.common.C
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.ext.basePath
import io.nichijou.tujian.common.ext.saveToAlbum
import io.nichijou.tujian.common.ext.toClipboard
import kotlinx.android.parcel.Parcelize
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

@Entity(tableName = "tb_picture", indices = [Index(value = ["pid", "from"], unique = true)])
@JsonClass(generateAdapter = true)
@Parcelize
data class Picture(
  @PrimaryKey
  @Json(name = "PID")
  val pid: String, // 88cb57b1-7dea-11e9-9337-f23c914b97eb
  @Json(name = "local_url")
  var local: String, // https://img.dpic.dev/e90004488aa287882f86a4a2e7c6700c
  @Json(name = "p_content")
  val desc: String, // 今年もどうぞよろしくお願いします！来源：Pixiv id 72390505//凌波超可爱！
  @Json(name = "p_date")
  val date: String, // 2019-05-25
  @Json(name = "p_link")
  val link: String, // https://img.dpic.dev/e90004488aa287882f86a4a2e7c6700c
  @Json(name = "p_title")
  val title: String, // 【綾波(アズールレーン)】「花火」/「ひみつ」
  @Json(name = "username")
  val user: String, // IceFex
  @Json(name = "width")
  val width: Int, // 1180
  @Json(name = "height")
  val height: Int, // 2150
  @Json(name = "TID")
  val tid: String? = null, // e5771003-b4ed-11e8-a8ea-0202761b0892
  @Json(name = "T_NAME")
  val category: String? = null, // 电脑壁纸
  @Json(name = "level")
  val level: Int = 1, // 1
  @Json(name = "nativePath")
  val nativePath: String,
  var from: Int = FROM_BROWSE// 0 浏览 1 wallpaper
) : BaseEntity(), Parcelable {
  fun share() = "标题：$title via $user\n" +
    "日期：$date\n" +
    "描述：$desc\n" +
    "分辨率：$width × $height\n" +
    "下载地址：$local"

  fun copy(context: Context) {
    context.toClipboard(share())
    context.toast("$title via $user")
  }

  fun download(context: Context) {
    context.toast("开始下载原图...")
    val name = "Tujian-" + title + Date()
    Glide.with(context).asBitmap().load(getNewUrl(this)).into(object : CustomTarget<Bitmap>() {
      override fun onLoadCleared(placeholder: Drawable?) {}
      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        resource.saveToAlbum(context, name)
      }
    })
  }

  companion object {
    const val FROM_BROWSE = 0
    const val FROM_WALLPAPER = 1
    const val FROM_MUZEI = 2
    const val FROM_APPWIDGET = 3
    const val FROM_BING = 4
  }
}

// tujian v2 API
fun getNewUrl(picture: Picture?): String? {
  return if (picture?.nativePath == picture?.local) picture?.local else C.API_SS + picture?.nativePath
}
