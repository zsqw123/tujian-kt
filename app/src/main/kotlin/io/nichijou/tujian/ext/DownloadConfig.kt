package io.nichijou.tujian.ext

import android.app.DownloadManager
import com.chibatching.kotpref.KotprefModel

object DownloadConfig : KotprefModel() {
  /**
   * 1、Request.VISIBILITY_VISIBLE
   * 在下载进行的过程中，通知栏中会一直显示该下载的Notification，当下载完成时，该Notification会被移除，这是默认的参数值。
   * 2、Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
   * 在下载过程中通知栏会一直显示该下载的Notification，在下载完成后该Notification会继续显示，直到用户点击该Notification或者消除该Notification。
   * 3、Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION
   * 只有在下载完成后该Notification才会被显示。
   * 4、Request.VISIBILITY_HIDDEN
   * 不显示该下载请求的Notification。如果要使用这个参数，需要在应用的清单文件中加上DOWNLOAD_WITHOUT_NOTIFICATION权限。
   */
  var notificationVisibility: Int by intPref(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
  /**
   * 允许下载的网络类型 NETWORK_WIFI、NETWORK_MOBILE、NETWORK_MOBILE or NETWORK_WIFI
   */
  var allowedNetworkTypes: Int by intPref(DownloadManager.Request.NETWORK_WIFI)
  /**
   * 是否允许被系统扫描到
   */
  var allowScanningByMediaScanner: Boolean by booleanPref(true)
  /**
   * 在系统downloads中是否可见，即能否被系统downloads扫描到
   */
  var visibleInDownloadsUi: Boolean by booleanPref(false)
  /**
   * 是否允许漫游下载
   */
  var allowedOverRoaming: Boolean by booleanPref(false)
  /**
   * 充电情况下进行任务
   */
  var requiresCharging: Boolean by booleanPref(false)
  /**
   * 流量超出时是否下载
   */
  var allowedOverMetered: Boolean by booleanPref(false)
  /**
   * 要求设备没有在被使用时下载
   */
  var requiresDeviceIdle: Boolean by booleanPref(false)
}
