package io.nichijou.viewer.internal

import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import com.facebook.drawee.view.SimpleDraweeView
import io.nichijou.viewer.ViewerItemView

internal class ViewerConfig<T>(
  val images: List<T>,
  val pagerLoader: (view: ViewerItemView, data: T) -> Unit,
  val bgLoader: (view: SimpleDraweeView, data: T) -> Unit
) {
  var backgroundColor = Color.BLACK
  var startPosition: Int = 0
  var imageChangeListener: ((pos: Int) -> Unit)? = null
  var onDismissListener: (() -> Unit)? = null
  var overlayView: View? = null
  var imageMarginPixels: Int = 0
  var containerPaddingPixels = IntArray(4)
  var transitionView: View? = null
  var onOpenStart: ((duration: Long) -> Unit)? = null
  var onOpenEnd: (() -> Unit)? = null
  var onCloseStart: ((duration: Long) -> Unit)? = null
  var onCloseEnd: (() -> Unit)? = null
  var onSingleTap: ((MotionEvent) -> Unit)? = null
  var onViewSwipe: ((translationY: Float, translationLimit: Int) -> Unit)? = null
}
