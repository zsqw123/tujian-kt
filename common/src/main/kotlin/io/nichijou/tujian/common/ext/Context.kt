package io.nichijou.tujian.common.ext

import android.annotation.*
import android.app.*
import android.content.*
import android.content.pm.*
import android.net.*
import android.os.*
import android.util.*
import androidx.annotation.*
import androidx.core.graphics.drawable.*
import androidx.palette.graphics.*
import java.io.*


@Px
fun Context.getStatusBarHeight() = this.resources.getDimensionPixelSize(this.resources.getIdentifier("status_bar_height", "dimen", "android"))

@Px
fun Context.getScreenHeight(): Int = this.resources.displayMetrics.heightPixels

@Px
fun Context.getScreenWidth(): Int = this.resources.displayMetrics.widthPixels

fun Context.dp2px(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics) + 0.5f

fun Context.px2dp(px: Float): Float = px / this.resources.displayMetrics.density + 0.5f

fun Context.px2sp(px: Float): Float = px / this.resources.displayMetrics.scaledDensity + 0.5f

fun Context.sp2px(sp: Float): Float = sp * this.resources.displayMetrics.scaledDensity + 0.5f


fun Context.appInstalled(packageName: String): Boolean {
  val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
  val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
  return list.isNotEmpty()
}

fun Context.basePath(dir: String = ""): String {
//  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
  return "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath}${File.separator}tujian${File.separator}$dir"

}

fun Context.toClipboard(text: String, label: String = "label") {
  val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  cm.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun Context.openApp(packageName: String): Boolean {
  try {
    val intent = this.packageManager.getLaunchIntentForPackage(packageName) ?: return false
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP
    this.startActivity(intent)
  } catch (e: Exception) {
    return false
  }
  return true
}

fun Context.openUrl(url: String) {
  try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
  } catch (e: Exception) {
  }
}

fun Context.setAsFromImage(file: File) {
  val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    putExtra(Intent.EXTRA_MIME_TYPES, "image/*")
    data = file.toURI(this@setAsFromImage)
  }
  startActivity(Intent.createChooser(intent, "SET AS"))
}

fun Context.sendFromImage(file: File) {
  val intent = Intent(Intent.ACTION_SEND).apply {
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    type = "image/*"
    putExtra(Intent.EXTRA_STREAM, file.toURI(this@sendFromImage))
  }
  startActivity(Intent.createChooser(intent, "SEND"))
}

fun Context.shareString(text: String?) {
  val intent = Intent(Intent.ACTION_SEND).apply {
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, text)
  }
  startActivity(Intent.createChooser(intent, "SHARE"))
}

fun Context.openVideoPlayer(url: String?) {
  if (url == null) {
    applicationContext.toast("url is null.")
    return
  }
  val uri = Uri.parse(url)
  if (uri == null) {
    applicationContext.toast("can't resolve url: $url")
    return
  }
  val intent = Intent()
  intent.action = Intent.ACTION_VIEW
  intent.setDataAndType(uri, "video/*")
  startActivity(intent)
}

fun Context.getNavigationBarHeight(): Int {
  val resourceId = this.resources.getIdentifier("navigation_bar_height", "dimen", "android")
  return if (resourceId > 0) {
    this.resources.getDimensionPixelSize(resourceId)
  } else {
    0
  }
}

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
fun Context.isNavigationBarEnabled(): Boolean {
  var hasNavBar = false
  try {
    val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
    val getWmServiceMethod = windowManagerGlobalClass.getDeclaredMethod("getWindowManagerService")
    getWmServiceMethod.isAccessible = true
    val iWindowManager = getWmServiceMethod.invoke(null)
    val hasNavBarMethod = iWindowManager::class.java.getDeclaredMethod("hasNavigationBar")
    hasNavBarMethod.isAccessible = true
    return hasNavBarMethod.invoke(iWindowManager) as Boolean
  } catch (e: Exception) {
    loge("first: check nav bar error.", e)
  }
  val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
  if (id > 0) {
    hasNavBar = resources.getBoolean(id)
  }
  try {
    val systemPropertiesClass = Class.forName("android.os.SystemProperties")
    val m = systemPropertiesClass.getMethod("get", String::class.java)
    val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as? String?
    if ("1" == navBarOverride) {
      hasNavBar = false
    } else if ("0" == navBarOverride) {
      hasNavBar = true
    }
  } catch (e: Exception) {
    loge("second: check nav bar error.", e)
  }
  return hasNavBar
}

fun Context.getWallpaperPrimaryColorCompat(): Int {
  val manager = WallpaperManager.getInstance(this)
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
    manager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb()
      ?: getLegacyWallpaperPrimaryColor()
  } else {
    getLegacyWallpaperPrimaryColor()
  }
}

fun Context.getLegacyWallpaperPrimaryColor(): Int {
  val drawable = WallpaperManager.getInstance(this).drawable ?: return 0
  val div = (drawable.intrinsicHeight * drawable.intrinsicWidth).toFloat() / (getScreenHeight() * getScreenWidth()).toFloat()
  val scale = if (div > 1f) div else 1f
  val bitmap = drawable.toBitmap((drawable.intrinsicWidth.toFloat() / scale).toInt(), (drawable.intrinsicHeight.toFloat() / scale).toInt())
  return Palette.from(bitmap).generate().dominantSwatch?.rgb ?: 0
}

fun Context.readAssetsFileText(fileName: String): String {
  val inputStream = this.assets.open(fileName)
  val result = inputStream.use { input ->
    var offset = 0
    var remaining = input.available().also { length ->
      if (length > Int.MAX_VALUE) throw OutOfMemoryError("File $this is too big ($length bytes) to fit in memory.")
    }.toInt()
    val result = ByteArray(remaining)
    while (remaining > 0) {
      val read = input.read(result, offset, remaining)
      if (read < 0) break
      remaining -= read
      offset += read
    }
    if (remaining == 0) result else result.copyOf(offset)
  }
  return result.toString(Charsets.UTF_8)
}
