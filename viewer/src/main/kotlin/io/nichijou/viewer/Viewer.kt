package io.nichijou.viewer

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.facebook.drawee.view.SimpleDraweeView
import io.nichijou.viewer.internal.ViewerConfig
import io.nichijou.viewer.internal.ViewerView
import kotlin.math.roundToInt

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

  fun currentItem(): T = viewerView.currentItem()

  fun currentPosition(): Int = viewerView.currentPosition

  fun show(): Viewer<T> {
    if (!viewerConfig.images.isNullOrEmpty()) {
      dialog.show()
    }
    return this
  }

  fun dismiss() {
    viewerView.close()
  }

  fun updateImages(images: Array<T>) {
    updateImages(listOf(*images))
  }

  fun updateImages(images: List<T>?) {
    if (!images.isNullOrEmpty()) {
      viewerView.updateImages(images)
    } else {
      viewerView.close()
    }
  }

  class Builder<T>(private val context: Context, images: List<T>?, pagerLoader: (view: ViewerItemView, data: T) -> Unit, bgLoader: (view: SimpleDraweeView, data: T) -> Unit) {
    private val data: ViewerConfig<T> = ViewerConfig(images ?: emptyList(), pagerLoader, bgLoader)

    fun withStartPosition(position: Int): Builder<T> {
      this.data.startPosition = position
      return this
    }

    fun withBackgroundColor(@ColorInt color: Int): Builder<T> {
      this.data.backgroundColor = color
      return this
    }

    fun withOnOpen(onOpenStart: ((duration: Long) -> Unit)? = null, onOpenEnd: (() -> Unit)? = null): Builder<T> {
      this.data.onOpenStart = onOpenStart
      this.data.onOpenEnd = onOpenEnd
      return this
    }

    fun withOnClose(onCloseStart: ((duration: Long) -> Unit)? = null, onCloseEnd: (() -> Unit)? = null): Builder<T> {
      this.data.onCloseStart = onCloseStart
      this.data.onCloseEnd = onCloseEnd
      return this
    }

    fun withOnSingleTap(onSingleTap: ((MotionEvent) -> Unit)? = null): Builder<T> {
      this.data.onSingleTap = onSingleTap
      return this
    }

    fun withOnViewSwipe(onViewSwipe: ((translationY: Float, translationLimit: Int) -> Unit)? = null): Builder<T> {
      this.data.onViewSwipe = onViewSwipe
      return this
    }

    fun withBackgroundColorResource(@ColorRes color: Int): Builder<T> {
      return this.withBackgroundColor(ContextCompat.getColor(context, color))
    }

    fun withOverlayView(view: View): Builder<T> {
      this.data.overlayView = view
      return this
    }

    fun withImagesMargin(@DimenRes dimen: Int): Builder<T> {
      this.data.imageMarginPixels = Math.round(context.resources.getDimension(dimen))
      return this
    }

    fun withImageMarginPixels(marginPixels: Int): Builder<T> {
      this.data.imageMarginPixels = marginPixels
      return this
    }

    fun withContainerPadding(@DimenRes padding: Int): Builder<T> {
      val paddingPx = context.resources.getDimension(padding).roundToInt()
      return withContainerPaddingPixels(paddingPx, paddingPx, paddingPx, paddingPx)
    }

    fun withContainerPadding(
      @DimenRes start: Int,
      @DimenRes top: Int,
      @DimenRes end: Int,
      @DimenRes bottom: Int
    ): Builder<T> {
      withContainerPaddingPixels(
        context.resources.getDimension(start).roundToInt(),
        context.resources.getDimension(top).roundToInt(),
        context.resources.getDimension(end).roundToInt(),
        context.resources.getDimension(bottom).roundToInt()
      )
      return this
    }

    fun withContainerPaddingPixels(@Px padding: Int): Builder<T> {
      this.data.containerPaddingPixels = intArrayOf(padding, padding, padding, padding)
      return this
    }

    fun withContainerPaddingPixels(start: Int, top: Int, end: Int, bottom: Int): Builder<T> {
      this.data.containerPaddingPixels = intArrayOf(start, top, end, bottom)
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

    fun withDismissListener(onDismissListener: () -> Unit): Builder<T> {
      this.data.onDismissListener = onDismissListener
      return this
    }

    fun build(): Viewer<T> = Viewer(context, data)

    fun show(): Viewer<T> = build().show()

  }
}
