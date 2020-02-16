package com.yarolegovich.slidingrootnav.transform

import android.view.View
import com.yarolegovich.slidingrootnav.util.slideEvaluate

class ScaleTransformation(private val endScale: Float) : RootTransformation {

  override fun transform(dragProgress: Float, rootView: View) {
    val scale = slideEvaluate(dragProgress, START_SCALE, endScale)
    rootView.scaleX = scale
    rootView.scaleY = scale
  }

  companion object {
    private const val START_SCALE = 1f
  }
}
