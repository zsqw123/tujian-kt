package com.yarolegovich.slidingrootnav.util

import android.annotation.*
import android.content.*
import android.view.*
import com.yarolegovich.slidingrootnav.*


@SuppressLint("ViewConstructor")
class HiddenMenuClickConsumer(context: Context, private val drawer: SlidingDrawerLayout) : View(context) {
  override fun onTouchEvent(event: MotionEvent): Boolean {
    return !drawer.isMenuOpened()
  }
}
