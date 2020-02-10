package com.yarolegovich.slidingrootnav.transform

import android.view.*
import com.yarolegovich.slidingrootnav.util.*


class YTranslationTransformation(private val endTranslation: Float) : RootTransformation {

  override fun transform(dragProgress: Float, rootView: View) {
    rootView.translationY = slideEvaluate(dragProgress, START_TRANSLATION, endTranslation)
  }

  companion object {
    private const val START_TRANSLATION = 0f
  }
}
