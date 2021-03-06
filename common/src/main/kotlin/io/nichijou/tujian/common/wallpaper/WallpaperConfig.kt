package io.nichijou.tujian.common.wallpaper

import com.chibatching.kotpref.KotprefModel
import java.util.concurrent.TimeUnit

object WallpaperConfig : KotprefModel() {
  var notification: Boolean by booleanPref(true)
  var requiresBatteryNotLow: Boolean by booleanPref(false)
  var requiresCharging: Boolean by booleanPref(false)
  var requiresStorageNotLow: Boolean by booleanPref(false)
  var requiresDeviceIdle: Boolean by booleanPref(false)
  var enable: Boolean by booleanPref(false)
  var interval: Long by longPref(TimeUnit.MINUTES.toMillis(15))
  var categoryId: String by stringPref("")
  var blur: Boolean by booleanPref(false)
  var pixel: Boolean by booleanPref(false)
  var blurValue: Int by intPref(2500)
  var pixelValue: Int by intPref(2400)
}
