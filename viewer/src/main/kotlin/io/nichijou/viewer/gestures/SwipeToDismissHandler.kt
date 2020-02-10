package io.nichijou.viewer.gestures

import android.annotation.*
import android.view.*
import android.view.animation.*
import io.nichijou.viewer.ext.*
import kotlin.math.*

class SwipeToDismissHandler(
  private val swipeView: View,
  private val onDismiss: () -> Unit,
  private val onSwipeViewMove: (translationY: Float, translationLimit: Int) -> Unit,
  private val shouldAnimateDismiss: () -> Boolean
) : View.OnTouchListener {

  private var translationLimit: Int = swipeView.height / 4
  private var isTracking = false
  private var startY: Float = 0f
  private var xCoOrdinate: Float = 0f
  private var yCoOrdinate: Float = 0f
  private val screenCenterX = swipeView.context.resources.displayMetrics.widthPixels / 2.0
  private val screenCenterY = swipeView.context.resources.displayMetrics.heightPixels / 2.0
  private val maxHypo = hypot(screenCenterX, screenCenterY)
  private var scale = 0f

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouch(v: View, event: MotionEvent): Boolean {
    val a = screenCenterX - (swipeView.x + swipeView.width / 2)
    val b = screenCenterY - (swipeView.y + swipeView.height / 2)
    val hypo = hypot(a, b)
    scale = (1.0 - hypo / maxHypo / 2).toFloat()
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        if (swipeView.hitRect.contains(event.x.toInt(), event.y.toInt())) {
          isTracking = true
          xCoOrdinate = swipeView.x - event.rawX
          yCoOrdinate = swipeView.y - event.rawY
        }
        startY = event.y
        return true
      }
      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        if (isTracking) {
          isTracking = false
          swipeView.animate().scaleX(1f).scaleY(1f)
            .x(0f)/*.y((screenCenterY - swipeView.height / 2f).toFloat())*/.setDuration(ANIMATION_DURATION)
            .start()
          onTrackingEnd(v.height)
        }
        return true
      }
      MotionEvent.ACTION_MOVE -> {
        if (isTracking) {
          val translationY = event.y - startY
          swipeView.translationY = translationY
          swipeView.animate().scaleX(scale).scaleY(scale)
            .x(event.rawX + xCoOrdinate)/*.y(event.rawY + yCoOrdinate)*/.setDuration(0).start()
          onSwipeViewMove(translationY, translationLimit)
        }
        return true
      }
      else -> {
        return false
      }
    }
  }

  fun initiateDismissToBottom() {
    animateTranslation(swipeView.height.toFloat(), ANIMATION_DURATION)
  }

  private fun onTrackingEnd(parentHeight: Int) {
    val animateTo = when {
      swipeView.translationY < -translationLimit -> -parentHeight.toFloat()
      swipeView.translationY > translationLimit -> parentHeight.toFloat()
      else -> 0f
    }

    if (animateTo != 0f && !shouldAnimateDismiss()) {
      onDismiss()
    } else {
      animateTranslation(animateTo, ANIMATION_DURATION)
    }
  }

  private fun animateTranslation(translationTo: Float, duration: Long) {
    swipeView.animate()
      .translationY(translationTo)
      .setDuration(duration)
      .setInterpolator(AccelerateInterpolator())
      .setUpdateListener { onSwipeViewMove(swipeView.translationY, translationLimit) }
      .setAnimatorListener(onAnimationEnd = {
        if (translationTo != 0f) {
          onDismiss()
        }
        swipeView.animate().setUpdateListener(null)
      })
      .start()
  }

  companion object {
    private const val ANIMATION_DURATION = 200L
  }
}
