package io.nichijou.viewer.ext

import android.content.*

internal fun Context.getScreenHeight(): Int = this.resources.displayMetrics.heightPixels

internal fun Context.getScreenWidth(): Int = this.resources.displayMetrics.widthPixels
