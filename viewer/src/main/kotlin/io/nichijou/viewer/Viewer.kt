package io.nichijou.viewer

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.facebook.drawee.view.SimpleDraweeView
import io.nichijou.viewer.internal.ViewerConfig
import io.nichijou.viewer.internal.ViewerView

class Viewer<T> private constructor(context: Context, private val viewerConfig: ViewerConfig<T>) {

  private val dialog: AlertDialog
  private val viewerView: ViewerView<T> = ViewerView(context)

  init {
    setupViewerView()
    dialog = AlertDialog
      .Builder(context, R.style.ViewerStyle)
      .setView(viewerView)
      .setOnKeyListener { _, keyCode, event ->
        onDialogKeyEvent(keyCode, event)
      }
      .create()
      .apply {
        setOnShowListener { viewerView.open(viewerConfig.transitionView) }
        setOnDismissListener { viewerConfig.onDismissListener?.invoke() }
      }
  }


  fun updateTransitionImage(imageView: ImageView?) {
    viewerView.updateTransitionImage(imageView)
  }

  private fun onDialogKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
    if (keyCode == KeyEvent.KEYCODE_BACK &&
      event.action == KeyEvent.ACTION_UP &&
      !event.isCanceled
    ) {
      if (viewerView.isScaled) {
        viewerView.resetScale()
      } else {
        viewerView.close()
      }
    }
    return true
  }

  private fun setupViewerView() {
    viewerView.apply {
      containerPadding = viewerConfig.containerPaddingPixels
      overlayView = viewerConfig.overlayView
      onOpenStart = viewerConfig.onOpenStart
      onOpenEnd = viewerConfig.onOpenEnd
      onCloseStart = viewerConfig.onCloseStart
      onCloseEnd = viewerConfig.onCloseEnd
      onSingleTap = viewerConfig.onSingleTap
      onViewSwipe = viewerConfig.onViewSwipe
      setBackgroundColor(viewerConfig.backgroundColor)
      setImages(viewerConfig.images, viewerConfig.startPosition, viewerConfig.pagerLoader, viewerConfig.bgLoader)
      onPageChange = { position -> viewerConfig.imageChangeListener?.invoke(position) }
      onDismiss = {
        dialog.dismiss()
      }
    }
  }

  fun show(): Viewer<T> {
    if (!viewerConfig.images.isNullOrEmpty()) {
      dialog.show()
    }
    return this
  }

  fun updateImages(images: List<T>?) {
    if (!images.isNullOrEmpty()) {
      viewerView.updateImages(images)
    } else {
      viewerView.close()
    }
  }

  class Builder<T>(
    private val context: Context,
    images: List<T>?,
    pagerLoader: (view: ViewerItemView, data: T) -> Unit,
    bgLoader: (view: SimpleDraweeView, data: T) -> Unit
  ) {

    private val data: ViewerConfig<T> = ViewerConfig(images ?: emptyList(), pagerLoader, bgLoader)

    fun withStartPosition(position: Int): Builder<T> {
      this.data.startPosition = position
      return this
    }

    fun withOnSingleTap(onSingleTap: ((MotionEvent) -> Unit)? = null): Builder<T> {
      this.data.onSingleTap = onSingleTap
      return this
    }

    fun withImageMarginPixels(marginPixels: Int): Builder<T> {
      this.data.imageMarginPixels = marginPixels
      return this
    }

    fun withTransitionFrom(imageView: View): Builder<T> {
      this.data.transitionView = imageView
      return this
    }

    fun withImageChangeListener(imageChangeListener: (pos: Int) -> Unit): Builder<T> {
      this.data.imageChangeListener = imageChangeListener
      return this
    }

    fun build(): Viewer<T> = Viewer(context, data)

    fun show(): Viewer<T> = build().show()

  }
}
