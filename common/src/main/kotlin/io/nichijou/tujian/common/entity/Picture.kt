package io.nichijou.tujian.common.entity

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.afollestad.assent.Permission
import com.afollestad.assent.isAllGranted
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.ext.basePath
import io.nichijou.tujian.common.ext.toClipboard
import kotlinx.android.parcel.Parcelize
import org.jetbrains.anko.toast
import java.io.File
import java.util.*


// 图片
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
//  @Json(name = "nativePath")
//  val nativePath: String,
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
    if (context.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
      val uri = Uri.parse(local) ?: return
      val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
      val request = DownloadManager.Request(uri)
      val fileName = title + " - " + Date() + ".jpg"
      val dirPath = context.basePath()
      val dir = File(dirPath)
      if (!dir.exists()) {
        dir.mkdirs()
      }
      request.setDestinationInExternalPublicDir(dirPath.substring(dirPath.indexOf("tujian")), fileName)
      request.setTitle("图鉴•$fileName")
      request.setDescription("$desc\n via $user $date")
      request.allowScanningByMediaScanner()
      request.setVisibleInDownloadsUi(true)
      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
      request.setMimeType("image/*")
      manager.enqueue(request)
      context.toast(R.string.start_download)
    } else {
      context.toast(R.string.no_permission_for_download)
    }
  }

  companion object {
    const val FROM_BROWSE = 0
    const val FROM_WALLPAPER = 1
    const val FROM_MUZEI = 2
    const val FROM_APPWIDGET = 3
    const val FROM_BING = 4
  }
}
