package com.larvalabs.boo

import android.view.animation.*

class EaseOutElasticInterpolator : Interpolator {

  override fun getInterpolation(value: Float): Float {
    val s = 0.3f / 4.0f
    //return (float) (Math.pow(2.0, -10.0d * value) * Math.sin((value - s) * (PI_TIMES_2) / 0.3d) + 1.0d);
    // This version is a little less violent than the standard formula
    return (Math.pow(2.0, -15.0 * value.toDouble() * value.toDouble()) * Math.sin((value * value - s) * PI_TIMES_2 / 0.3) + 1.0).toFloat()
  }

  companion object {

    private const val PI_TIMES_2 = (Math.PI * 2).toFloat()
  }

}
