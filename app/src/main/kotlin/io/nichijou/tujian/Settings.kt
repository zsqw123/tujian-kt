package io.nichijou.tujian

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.chibatching.kotpref.KotprefModel
import io.nichijou.oops.KEY_COLOR_ACCENT
import io.nichijou.oops.PREFS_NAME
import org.jetbrains.anko.configuration

object Settings : KotprefModel() {
  var screenSaverInterval: Long by longPref(8 * 1000)// 8秒
  var enableFaceDetection: Boolean by booleanPref(false)
  var fuckBoo: Boolean by booleanPref(false)
  var topBarRadius: Int by intPref(1600)
  var topBarElevation: Int by intPref(1200)
  var cardRadius: Int by intPref(400)
  var cardElevation: Int by intPref(1200)
  var cardSpace: Int by intPref(400)
  var creatureNum: Int by intPref(1000)// 小精灵数
  var darkModeInt: Int by intPref(2)
  var feiHua: Boolean by booleanPref(false)// 是否同意废话
}

object UserData : KotprefModel() {
  var email: String by stringPref()
  var name: String by stringPref()
}

fun isDark(): Boolean {
  return when (Settings.darkModeInt) {
    0 -> true
    1 -> false
    else -> {
      when (App.context!!.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO -> {
          false
        } // Night mode is not active, we're using the light theme
        else -> true
      }
    }
  }
}

fun getThemeColor(): Int = App.context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  .getInt(KEY_COLOR_ACCENT, Color.BLACK)
