package io.nichijou.tujian

import com.chibatching.kotpref.*

object Settings : KotprefModel() {
  var screenSaverInterval: Long by longPref(8 * 1000)// 8秒
  var enableFaceDetection: Boolean by booleanPref(true)
  var fuckBoo: Boolean by booleanPref(true)
  var topBarRadius: Int by intPref(0)
  var topBarElevation: Int by intPref(1200)
  var cardRadius: Int by intPref(0)
  var cardElevation: Int by intPref(1200)
  var cardSpace: Int by intPref(400)
  var creatureNum: Int by intPref(1000)
  var darkModeInt: Int by intPref(2)
}
