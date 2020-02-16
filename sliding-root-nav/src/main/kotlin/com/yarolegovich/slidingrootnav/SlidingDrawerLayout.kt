package com.yarolegovich.slidingrootnav

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import com.yarolegovich.slidingrootnav.transform.RootTransformation
import kotlin.math.abs

class SlidingDrawerLayout(context: Context) : FrameLayout(context) {

  private val FLING_MIN_VELOCITY: Float = ViewConfiguration.get(context).scaledMinimumFlingVelocity.toFloat()

  private var isMenuLocked: Boolean = false
  private var isMenuClosed: Boolean = true

  private var isContentClickableWhenMenuOpened: Boolean = false

  private lateinit var rootTransformation: RootTransformation

  var dragProgress: Float = 0f

  private var maxDragDistance: Int = 0
  private var dragState: Int = 0

  private val dragHelper: ViewDragHelper = ViewDragHelper.create(this, ViewDragCallback())
  private var positionHelper: SlideGravity.Helper = SlideGravity.START.createHelper()

  private val drawerListeners = arrayListOf<DrawerLayout.DrawerListener>()

  fun isMenuOpened(): Boolean = !isMenuClosed


  fun isMenuLocked(): Boolean = isMenuLocked

  fun setMenuLocked(isLocked: Boolean) {
    this.isMenuLocked = isLocked
  }

  val layout: SlidingDrawerLayout = this

  lateinit var contentView: View
  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return !isMenuLocked && dragHelper.shouldInterceptTouchEvent(ev) || shouldBlockClick(ev)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    dragHelper.processTouchEvent(event)
    return true
  }

  private var dispatchTouchListener: ((MotionEvent?) -> Unit)? = null

  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    dispatchTouchListener?.invoke(ev)
    return super.dispatchTouchEvent(ev)
  }

  fun setDispatchTouch(dispatch: (MotionEvent?) -> Unit) {
    this.dispatchTouchListener = dispatch
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      if (child === contentView) {
        val rootLeft = positionHelper.getRootLeft(dragProgress, maxDragDistance)
        child.layout(rootLeft, top, rootLeft + (right - left), bottom)
      } else {
        child.layout(left, top, right, bottom)
      }
    }
  }

  override fun computeScroll() {
    if (dragHelper.continueSettling(true)) {
      ViewCompat.postInvalidateOnAnimation(this)
    }
  }

  private fun changeMenuVisibility(animated: Boolean, newDragProgress: Float) {
    isMenuClosed = calculateIsMenuHidden()
    if (animated) {
      val left = positionHelper.getLeftToSettle(newDragProgress, maxDragDistance)
      if (dragHelper.smoothSlideViewTo(contentView, left, contentView.top)) {
        ViewCompat.postInvalidateOnAnimation(this)
      }
    } else {
      dragProgress = newDragProgress
      rootTransformation.transform(dragProgress, contentView)
      requestLayout()
    }
  }

  @JvmOverloads
  fun closeMenu(animated: Boolean = true) {
    changeMenuVisibility(animated, 0f)
  }

  @JvmOverloads
  fun openMenu(animated: Boolean = true) {
    changeMenuVisibility(animated, 1f)
  }

  fun setContentClickableWhenMenuOpened(contentClickableWhenMenuOpened: Boolean) {
    isContentClickableWhenMenuOpened = contentClickableWhenMenuOpened
  }

  fun setRootTransformation(transformation: RootTransformation) {
    rootTransformation = transformation
  }

  fun setMaxDragDistance(maxDragDistance: Int) {
    this.maxDragDistance = maxDragDistance
  }

  fun setGravity(gravity: SlideGravity) {
    positionHelper = gravity.createHelper()
    positionHelper.enableEdgeTrackingOn(dragHelper)
  }

  fun addDrawerListener(listener: DrawerLayout.DrawerListener) {
    drawerListeners.add(listener)
  }

  fun removeDrawerListener(listener: DrawerLayout.DrawerListener) {
    drawerListeners.remove(listener)
  }

  private fun shouldBlockClick(event: MotionEvent): Boolean {
    if (isContentClickableWhenMenuOpened) {
      return false
    }
    if (isMenuOpened()) {
      contentView.getHitRect(tempRect)
      return tempRect.contains(event.x.toInt(), event.y.toInt())
    }
    return false
  }

  private fun notifyDrag() {
    for (listener in drawerListeners) {
      listener.onDrawerSlide(this, dragProgress)
    }
  }

  private fun notifyDragStart() {
    for (listener in drawerListeners) {
      listener.onDrawerStateChanged(DrawerLayout.STATE_DRAGGING)
    }
  }

  private fun notifyDragSettling() {
    for (listener in drawerListeners) {
      listener.onDrawerStateChanged(DrawerLayout.STATE_SETTLING)
    }
  }

  private fun notifyDragEnd(isOpened: Boolean) {
    for (listener in drawerListeners) {
      if (isOpened) {
        listener.onDrawerOpened(this)
      } else {
        listener.onDrawerClosed(this)
      }
      listener.onDrawerStateChanged(DrawerLayout.STATE_IDLE)
    }
  }

  override fun onSaveInstanceState(): Parcelable? {
    val savedState = Bundle()
    savedState.putParcelable(EXTRA_SUPER, super.onSaveInstanceState())
    savedState.putInt(EXTRA_IS_OPENED, if (dragProgress > 0.5) 1 else 0)
    savedState.putBoolean(EXTRA_SHOULD_BLOCK_CLICK, isContentClickableWhenMenuOpened)
    return savedState
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    val savedState = state as Bundle
    super.onRestoreInstanceState(savedState.getParcelable(EXTRA_SUPER))
    changeMenuVisibility(false, savedState.getInt(EXTRA_IS_OPENED, 0).toFloat())
    isMenuClosed = calculateIsMenuHidden()
    isContentClickableWhenMenuOpened = savedState.getBoolean(EXTRA_SHOULD_BLOCK_CLICK)
  }

  private fun calculateIsMenuHidden(): Boolean {
    return dragProgress == 0f
  }

  private inner class ViewDragCallback : ViewDragHelper.Callback() {

    private var edgeTouched: Boolean = false

    override fun tryCaptureView(child: View, pointerId: Int): Boolean {
      if (isMenuLocked) {
        return false
      }
      val isOnEdge = edgeTouched
      edgeTouched = false
      if (isMenuClosed) {
        return child === contentView && isOnEdge
      } else {
        if (child !== contentView) {
          dragHelper.captureChildView(contentView, pointerId)
          return false
        }
        return true
      }
    }

    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
      dragProgress = positionHelper.getDragProgress(left, maxDragDistance)
      rootTransformation.transform(dragProgress, contentView)
      notifyDrag()
      invalidate()
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
      val left = if (abs(xvel) < FLING_MIN_VELOCITY)
        positionHelper.getLeftToSettle(dragProgress, maxDragDistance)
      else
        positionHelper.getLeftAfterFling(xvel, maxDragDistance)
      dragHelper.settleCapturedViewAt(left, contentView.top)
      invalidate()
    }

    override fun onViewDragStateChanged(state: Int) {
      when {
        dragState != ViewDragHelper.STATE_SETTLING && state == ViewDragHelper.STATE_SETTLING -> notifyDragSettling()
        dragState == ViewDragHelper.STATE_IDLE && state != ViewDragHelper.STATE_IDLE -> notifyDragStart()
        dragState != ViewDragHelper.STATE_IDLE && state == ViewDragHelper.STATE_IDLE -> {
          isMenuClosed = calculateIsMenuHidden()
          notifyDragEnd(!isMenuClosed)
        }
      }
      dragState = state
    }

    override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
      edgeTouched = true
    }

    override fun getViewHorizontalDragRange(child: View): Int {
      return if (child === contentView) maxDragDistance else 0
    }

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
      return positionHelper.clampViewPosition(left, maxDragDistance)
    }
  }

  companion object {

    private const val EXTRA_IS_OPENED = "extra_is_opened"
    private const val EXTRA_SUPER = "extra_super"
    private const val EXTRA_SHOULD_BLOCK_CLICK = "extra_should_block_click"

    private val tempRect = Rect()
  }
}
