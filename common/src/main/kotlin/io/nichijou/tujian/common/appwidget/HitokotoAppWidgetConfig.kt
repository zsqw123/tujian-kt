package io.nichijou.tujian.common.appwidget

import com.chibatching.kotpref.KotprefModel
import java.util.concurrent.TimeUnit

object HitokotoAppWidgetConfig : KotprefModel() {
  var notification: Boolean by booleanPref(true)
  var requiresBatteryNotLow: Boolean by booleanPref(false)
  var requiresCharging: Boolean by booleanPref(false)
  var requiresDeviceIdle: Boolean by booleanPref(false)
  var enable: Boolean by booleanPref(false)
  var interval: Long by longPref(TimeUnit.MINUTES.toMillis(15))
  var hitokotoTextSize: Int by intPref(1400)
  var hitokotoLines: Int by intPref(300)
  var sourceTextSize: Int by intPref(1200)
  var autoTextColor: Boolean by booleanPref(false)
}
