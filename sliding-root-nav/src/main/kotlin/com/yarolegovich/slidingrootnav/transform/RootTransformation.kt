package com.yarolegovich.slidingrootnav.transform

import android.view.*

interface RootTransformation {
  fun transform(dragProgress: Float, rootView: View)
}
