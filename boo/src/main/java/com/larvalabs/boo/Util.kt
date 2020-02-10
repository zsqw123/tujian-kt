package com.larvalabs.boo

import android.util.*
import android.view.animation.*

object Util {

  val VERBOSE_ON = false
  val TAG = "unlookable"

  /**
   * Use for everything but entering/exiting..
   */
  val PATH_CURVE = PathInterpolator(0.4f, 0.0f, 0.2f, 1f)

  /**
   * Use for entering, or fading in.
   */
  val PATH_IN = PathInterpolator(0.0f, 0.0f, 0.2f, 1f)

  /**
   * Use for exiting, or fading out.
   */
  val PATH_OUT = PathInterpolator(0.4f, 0.0f, 1f, 1f)

  fun log(message: String) {
    Log.d(TAG, message)
  }

  fun verbose(message: String) {
    if (VERBOSE_ON) {
      Log.v(TAG, message)
    }
  }

  fun warn(message: String) {
    Log.w(TAG, message)
  }

  fun error(message: String) {
    Log.e(TAG, message)
  }

  fun error(e: Throwable) {
    Log.e(TAG, "", e)
  }

  fun error(message: String, e: Throwable) {
    Log.e(TAG, message, e)
  }

}
