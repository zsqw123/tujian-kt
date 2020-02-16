package com.yarolegovich.slidingrootnav.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.yarolegovich.slidingrootnav.SlidingDrawerLayout


@SuppressLint("ViewConstructor, ClickableViewAccessibility")
class HiddenMenuClickConsumer(context: Context, private val drawer: SlidingDrawerLayout) : View(context) {
  override fun onTouchEvent(event: MotionEvent): Boolean {
    return !drawer.isMenuOpened()
  }
}
