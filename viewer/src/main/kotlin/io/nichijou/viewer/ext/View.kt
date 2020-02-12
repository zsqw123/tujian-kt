package io.nichijou.viewer.ext

import android.animation.*
import android.graphics.*
import android.view.*

internal val View?.localVisibleRect: Rect
  get() = Rect().also { this?.getLocalVisibleRect(it) }
internal val View?.globalVisibleRect: Rect
  get() = Rect().also { this?.getGlobalVisibleRect(it) }
internal val View?.hitRect: Rect
  get() = Rect().also { this?.getHitRect(it) }
internal val View?.isRectVisible: Boolean
  get() = this != null && globalVisibleRect != localVisibleRect
internal val View?.isVisible: Boolean
  get() = this != null && visibility == View.VISIBLE

internal fun View.makeVisible() {
  visibility = View.VISIBLE
}

internal fun View.makeInvisible() {
  visibility = View.INVISIBLE
}

internal fun View.makeGone() {
  visibility = View.GONE
}

internal fun View.applyMargin(
  start: Int? = null,
  top: Int? = null,
  end: Int? = null,
  bottom: Int? = null
) {
  if (layoutParams is ViewGroup.MarginLayoutParams) {
    layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
      marginStart = start ?: marginStart
      topMargin = top ?: topMargin
      marginEnd = end ?: marginEnd
      bottomMargin = bottom ?: bottomMargin
    }
  }
}

internal fun View.requestNewSize(width: Int, height: Int) {
  layoutParams.width = width
  layoutParams.height = height
  layoutParams = layoutParams
}

internal fun View.animateAlpha(from: Float?, to: Float?, duration: Long) {
  alpha = from ?: 0f
  clearAnimation()
  animate()
    .alpha(to ?: 0f)
    .setDuration(duration)
    .start()
}

internal fun View.switchVisibilityWithAnimation() {
  val isVisible = visibility == View.VISIBLE
  val alphaFrom = if (isVisible) 1.0f else 0.0f
  val alphaTo = if (isVisible) 0.0f else 1.0f
  //  ObjectAnimator.ofFloat(this,"translationY",)
  ObjectAnimator.ofFloat(this, "alpha", alphaFrom, alphaTo).apply {
    duration = ViewConfiguration.getDoubleTapTimeout().toLong()

    if (isVisible) {
      addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
          makeGone()
        }
      })
    } else {
      makeVisible()
    }
    start()
  }
}

