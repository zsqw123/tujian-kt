package io.nichijou.tujian.common.ext

import android.util.Log
import io.nichijou.tujian.common.BuildConfig

inline fun <reified T> T.logd(info: String) {
//  if (BuildConfig.DEBUG) {
  Log.i(T::class.java.name + " => DEBUG", info)
//  }
}

inline fun <reified T> T.loge(info: String) {
//  if (BuildConfig.DEBUG) {
  Log.e(T::class.java.name + " => ERROR", info)
//  }
}

inline fun <reified T> T.loge(info: String, throwable: Throwable?) {
  if (BuildConfig.DEBUG) {
    Log.e(T::class.java.name + " => ERROR", info, throwable)
  }
}
