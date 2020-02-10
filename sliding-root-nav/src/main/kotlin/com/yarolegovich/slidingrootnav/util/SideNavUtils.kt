package com.yarolegovich.slidingrootnav.util

fun slideEvaluate(fraction: Float, startValue: Float, endValue: Float): Float {
  return startValue + fraction * (endValue - startValue)
}
