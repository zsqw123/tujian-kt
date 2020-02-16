package io.nichijou.tujian.func.muzei

import com.chibatching.kotpref.KotprefModel

object MuzeiConfig : KotprefModel() {
  var requiresBatteryNotLow: Boolean by booleanPref(false)
  var requiresCharging: Boolean by booleanPref(false)
  var requiresStorageNotLow: Boolean by booleanPref(false)
  var requiresDeviceIdle: Boolean by booleanPref(false)
  var categoryId: String by stringPref("")
}
