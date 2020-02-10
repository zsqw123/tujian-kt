package io.nichijou.tujian.ext

fun String.suffixRandom(): String {
  return "$this?${System.currentTimeMillis()}"
}
