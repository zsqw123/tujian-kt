package io.nichijou.viewer.internal

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import io.nichijou.viewer.ext.*

internal class TransitionImageAnimator(
  private val externalImage: View?,
  private val internalImage: View,
  private val internalImageContainer: FrameLayout
) {
  var isAnimating = false
  private var isClosing = false
  private val transitionDuration: Long
    get() = if (isClosing) TRANSITION_DURATION_CLOSE else TRANSITION_DURATION_OPEN
  private val internalRoot: ViewGroup
    get() = internalImageContainer.parent as ViewGroup

  fun animateOpen(
    containerPadding: IntArray,
    onTransitionStart: (Long) -> Unit,
    onTransitionEnd: () -> Unit
  ) {
    if (externalImage.isRectVisible) {
      onTransitionStart(TRANSITION_DURATION_OPEN)
      doOpenTransition(containerPadding, onTransitionEnd)
    } else {
      onTransitionEnd()
    }
  }

  fun animateClose(
    shouldDismissToBottom: Boolean,
    onTransitionStart: (Long) -> Unit,
    onTransitionEnd: () -> Unit
  ) {
    if (externalImage.isRectVisible && !shouldDismissToBottom) {
      onTransitionStart(TRANSITION_DURATION_CLOSE)
      doCloseTransition(onTransitionEnd)
    } else {
      externalImage?.visibility = View.VISIBLE
      onTransitionEnd()
    }
  }

  private fun doOpenTransition(containerPadding: IntArray, onTransitionEnd: () -> Unit) {
    isAnimating = true
    prepareTransitionLayout()

    internalRoot.post {
      apply {
        //防止闪烁
//      externalImage?.postDelayed(100) { visibility = View.INVISIBLE }

        TransitionManager.beginDelayedTransition(internalRoot, createTransition {
          if (!isClosing) {
            isAnimating = false
            onTransitionEnd()
          }
        })

        internalImageContainer.apply {
          applyMargin(0, 0, 0, 0)
          requestNewSize(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        }
//      if ((internalImage.width.toFloat() / internalImage.height.toFloat()) >= (context.getScreenWidth().toFloat() / context.getScreenHeight().toFloat())) {
//        internalImage.makeViewWidthMatchParent()
//      } else {
//        internalImage.makeViewHeightMatchParent()
//      }

        internalRoot.applyMargin(containerPadding[0], containerPadding[1], containerPadding[2], containerPadding[3])

        internalImageContainer.requestLayout()
      }
    }
  }

  private fun doCloseTransition(onTransitionEnd: () -> Unit) {
    isAnimating = true
    isClosing = true

    TransitionManager.beginDelayedTransition(internalRoot,
      createTransition { handleCloseTransitionEnd(onTransitionEnd) })

    prepareTransitionLayout()
    internalImageContainer.requestLayout()
  }

  private fun prepareTransitionLayout() {
    externalImage?.let {
      if (it.isRectVisible) {
        with(it.localVisibleRect) {
          internalImage.requestNewSize(it.width, it.height)
          internalImage.applyMargin(top = -top, start = -left)
        }
        with(it.globalVisibleRect) {
          internalImageContainer.requestNewSize(width(), height())
          internalImageContainer.applyMargin(left, top, right, bottom)
        }
      }
      resetRootTranslation()
    }
  }

  private fun handleCloseTransitionEnd(onTransitionEnd: () -> Unit) {
    externalImage?.visibility = View.VISIBLE
    externalImage?.post { onTransitionEnd() }
    isAnimating = false
  }

  private fun resetRootTranslation() {
//    internalRoot
//      .animate()
//      .translationY(0f)
//      .setDuration(transitionDuration)
//      .start()
  }

  private fun createTransition(onTransitionEnd: (() -> Unit)? = null): Transition =
    AutoTransition()
      .setDuration(transitionDuration)
      .setInterpolator(DecelerateInterpolator())
      .addListener(onTransitionEnd = { onTransitionEnd?.invoke() })

  companion object {
    private const val TRANSITION_DURATION_OPEN = 250L
    private const val TRANSITION_DURATION_CLOSE = 300L
  }
}
