package com.yarolegovich.slidingrootnav.transform

import android.view.View

interface RootTransformation {
  fun transform(dragProgress: Float, rootView: View)
}
