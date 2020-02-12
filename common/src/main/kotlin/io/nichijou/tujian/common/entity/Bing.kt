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
import com.squareup.moshi.*
import io.nichijou.tujian.common.R
import io.nichijou.tujian.common.ext.basePath
import io.nichijou.tujian.common.ext.toClipboard
import kotlinx.android.parcel.Parcelize
import org.jetbrains.anko.toast
import java.io.File


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
    context.toast(copyright)
  }

  fun download(context: Context) {
    if (context.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
      val uri = Uri.parse(url) ?: return
      val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
      val request = DownloadManager.Request(uri)
      val fileName = "$copyright - $date.jpg"
      val dirPath = context.basePath()
      val dir = File(dirPath)
      if (!dir.exists()) {
        dir.mkdirs()
      }
      request.setDestinationInExternalPublicDir(dirPath.substring(dirPath.indexOf("tujian")), fileName)
      request.setTitle("必应•$fileName")
      request.setDescription("$date - $copyright")
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
