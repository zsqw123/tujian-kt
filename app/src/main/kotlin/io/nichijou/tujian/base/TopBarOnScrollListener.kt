package io.nichijou.tujian.base

import android.view.*
import android.view.animation.*
import androidx.core.view.*
import androidx.recyclerview.widget.*

class TopBarOnScrollListener(private val barView: View) : RecyclerView.OnScrollListener() {
  private var mOffset = 0
  private var mControlsVisible = true
  private var mTotalScrolledDistance = 0

  private val barViewHeight by lazy {
    barView.height + barView.marginTop + barView.marginBottom
  }

  override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
    super.onScrollStateChanged(recyclerView, newState)
    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
      if (mTotalScrolledDistance < barViewHeight) {
        setVisible()
      } else {
        if (mControlsVisible) {
          if (mOffset > HIDE_THRESHOLD) {
            setInvisible()
          } else {
            setVisible()
          }
        } else {
          if (barViewHeight - mOffset > SHOW_THRESHOLD) {
            setVisible()
          } else {
            setInvisible()
          }
        }
      }
    }
  }

  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    super.onScrolled(recyclerView, dx, dy)
    clipOffset()
    onMoved(barView, mOffset)
    if (mOffset < barViewHeight && dy > 0 || mOffset > 0 && dy < 0) {
      mOffset += dy
    }
    if (mTotalScrolledDistance < 0) {
      mTotalScrolledDistance = 0
    } else {
      mTotalScrolledDistance += dy
    }
  }

  private fun clipOffset() {
    if (mOffset > barViewHeight) {
      mOffset = barViewHeight
    } else if (mOffset < 0) {
      mOffset = 0
    }
  }

  private fun setVisible() {
    if (mOffset > 0) {
      onShow(barView)
      mOffset = 0
    }
    mControlsVisible = true
  }

  private fun setInvisible() {
    if (mOffset < barViewHeight) {
      onHide(barView, barViewHeight)
      mOffset = barViewHeight
    }
    mControlsVisible = false
  }

  private fun onMoved(view: View, distance: Int) {
    view.translationY = -distance.toFloat()
  }

  private fun onShow(view: View) {
    view.animate().translationY(0f).setInterpolator(DecelerateInterpolator(2f)).start()
  }

  private fun onHide(view: View, height: Int) {
    view.animate().translationY(-height.toFloat()).setInterpolator(AccelerateInterpolator(2f))
      .start()
  }

  companion object {
    private const val HIDE_THRESHOLD = 10f
    private const val SHOW_THRESHOLD = 70f
  }
}
