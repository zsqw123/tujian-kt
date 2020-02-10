package io.nichijou.tujian.func.appwidget

import com.chibatching.kotpref.*

object BingAppWidgetConfig : KotprefModel() {
  var notification: Boolean by booleanPref(true)
  var requiresBatteryNotLow: Boolean by booleanPref(false)
  var requiresCharging: Boolean by booleanPref(false)
  var requiresStorageNotLow: Boolean by booleanPref(false)
  var requiresDeviceIdle: Boolean by booleanPref(false)
  var enable: Boolean by booleanPref(false)
  var blur: Boolean by booleanPref(false)
  var pixel: Boolean by booleanPref(false)
  var blurValue: Int by intPref(2500)
  var pixelValue: Int by intPref(2400)
  var textSize: Int by intPref(2600)
  var textLines: Int by intPref(300)
  var autoTextColor: Boolean by booleanPref(false)
}
