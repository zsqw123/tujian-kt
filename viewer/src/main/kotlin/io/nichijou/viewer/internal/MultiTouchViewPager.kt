package io.nichijou.viewer.internal

import android.annotation.*
import android.content.*
import android.util.*
import android.view.*
import androidx.viewpager.widget.*
import io.nichijou.viewer.ext.*

internal class MultiTouchViewPager @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : ViewPager(context, attrs) {
  var isIdle = true
    private set
  private var isInterceptionDisallowed: Boolean = false
  private var pageChangeListener: OnPageChangeListener? = null

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    pageChangeListener = addOnPageChangeListener(onPageScrollStateChanged = ::onPageScrollStateChanged)
  }

  override fun onDetachedFromWindow() {
    pageChangeListener?.let { removeOnPageChangeListener(it) }
    super.onDetachedFromWindow()
  }

  override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    isInterceptionDisallowed = disallowIntercept
    super.requestDisallowInterceptTouchEvent(disallowIntercept)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    return if (ev.pointerCount > 1 && isInterceptionDisallowed) {
      requestDisallowInterceptTouchEvent(false)
      val handled = super.dispatchTouchEvent(ev)
      requestDisallowInterceptTouchEvent(true)
      handled
    } else {
      super.dispatchTouchEvent(ev)
    }
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return if (ev.pointerCount > 1) {
      false
    } else {
      try {
        super.onInterceptTouchEvent(ev)
      } catch (ex: Exception) {
        false
      }
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(ev: MotionEvent): Boolean {
    return try {
      super.onTouchEvent(ev)
    } catch (ex: Exception) {
      false
    }
  }

  private fun onPageScrollStateChanged(state: Int) {
    isIdle = state == SCROLL_STATE_IDLE
  }
}
