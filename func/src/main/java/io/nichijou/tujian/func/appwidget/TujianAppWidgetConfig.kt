package io.nichijou.tujian.func.appwidget

import com.chibatching.kotpref.KotprefModel
import java.util.concurrent.TimeUnit

object TujianAppWidgetConfig : KotprefModel() {
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
  var hitokotoTextSize: Int by intPref(2600)// 需要减去最低字体大小12
  var hitokotoLines: Int by intPref(300)
  var sourceTextSize: Int by intPref(2400)// 需要减去最低字体大小12
  var autoTextColor: Boolean by booleanPref(false)
}
