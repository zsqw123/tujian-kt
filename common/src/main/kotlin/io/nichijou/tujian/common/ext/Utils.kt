package io.nichijou.tujian.common.ext

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import java.security.MessageDigest

fun Long.toDateStr(): String {
  val ss = 1000L
  val mi = ss * 60
  val hh = mi * 60
  val dd = hh * 24

  val day = this / dd
  val hour = (this - day * dd) / hh
  val minute = (this - day * dd - hour * hh) / mi
  val second = (this - day * dd - hour * hh - minute * mi) / ss
  var str = ""
  if (day > 0) {
    str += "$day 天"
  }
  if (hour > 0) {
    str += " $hour 小时"
  }
  if (minute > 0) {
    str += " $minute 分钟"
  }
  if (second > 0) {
    str += " $second 秒"
  }
  return str
}

fun String.md5(): String {
  val algorithm = "MD5"
  val digest = MessageDigest.getInstance(algorithm)
  digest.update(this.toByteArray())
  val mb = digest.digest()
  val hexString = StringBuilder()
  for (b in mb) {
    var h = Integer.toHexString(0xFF and b.toInt())
    while (h.length < 2) h = "0$h"
    hexString.append(h)
  }
  return hexString.toString()
}

fun Int.animateTo(to: Int, duration: Long = 720, update: (Int) -> Unit) {
  val animator = ValueAnimator.ofInt(this, to).setDuration(duration)
  animator.interpolator = AccelerateDecelerateInterpolator()
  animator.addUpdateListener {
    update.invoke(it.animatedValue as Int)
  }
  animator.start()
}
