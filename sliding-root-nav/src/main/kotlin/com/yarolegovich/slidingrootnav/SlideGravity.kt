package com.yarolegovich.slidingrootnav


import androidx.customview.widget.ViewDragHelper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class SlideGravity {

  START {
    override fun createHelper(): Helper {
      return LeftHelper()
    }
  },
  END {
    override fun createHelper(): Helper {
      return RightHelper()
    }
  };

  internal abstract fun createHelper(): Helper

  internal interface Helper {

    fun getLeftAfterFling(flingVelocity: Float, maxDrag: Int): Int

    fun getLeftToSettle(dragProgress: Float, maxDrag: Int): Int

    fun getRootLeft(dragProgress: Float, maxDrag: Int): Int

    fun getDragProgress(viewLeft: Int, maxDrag: Int): Float

    fun clampViewPosition(left: Int, maxDrag: Int): Int

    fun enableEdgeTrackingOn(dragHelper: ViewDragHelper)
  }

  internal class LeftHelper : Helper {

    override fun getLeftAfterFling(flingVelocity: Float, maxDrag: Int): Int {
      return if (flingVelocity > 0) maxDrag else 0
    }

    override fun getLeftToSettle(dragProgress: Float, maxDrag: Int): Int {
      return if (dragProgress > 0.5f) maxDrag else 0
    }

    override fun getRootLeft(dragProgress: Float, maxDrag: Int): Int {
      return (dragProgress * maxDrag).toInt()
    }

    override fun getDragProgress(viewLeft: Int, maxDrag: Int): Float {
      return viewLeft.toFloat() / maxDrag
    }

    override fun clampViewPosition(left: Int, maxDrag: Int): Int {
      return max(0, min(left, maxDrag))
    }

    override fun enableEdgeTrackingOn(dragHelper: ViewDragHelper) {
      dragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)
    }
  }

  internal class RightHelper : Helper {

    override fun getLeftAfterFling(flingVelocity: Float, maxDrag: Int): Int {
      return if (flingVelocity > 0) 0 else -maxDrag
    }

    override fun getLeftToSettle(dragProgress: Float, maxDrag: Int): Int {
      return if (dragProgress > 0.5f) -maxDrag else 0
    }

    override fun getRootLeft(dragProgress: Float, maxDrag: Int): Int {
      return (-(dragProgress * maxDrag)).toInt()
    }

    override fun getDragProgress(viewLeft: Int, maxDrag: Int): Float {
      return abs(viewLeft).toFloat() / maxDrag
    }

    override fun clampViewPosition(left: Int, maxDrag: Int): Int {
      return max(-maxDrag, min(left, 0))
    }

    override fun enableEdgeTrackingOn(dragHelper: ViewDragHelper) {
      dragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT)
    }
  }
}
