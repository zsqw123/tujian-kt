package io.nichijou.tujian.ui.today

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import io.nichijou.tujian.R
import io.nichijou.tujian.common.ext.getScreenHeight
import io.nichijou.tujian.common.ext.getScreenWidth
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import kotlinx.android.synthetic.main.view_today_item.view.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast

class TodayItemView(context: Context) : FrameLayout(context), SubsamplingScaleImageView.OnImageEventListener {

  private var onImageDownloaded: (() -> Unit)? = null
  private var downloaded = false
  private var hasAttachedWindow = false
  private val screenWidth by lazy(LazyThreadSafetyMode.NONE) { context.getScreenWidth().toFloat() }
  private val screenHeight by lazy(LazyThreadSafetyMode.NONE) { context.getScreenHeight().toFloat() }
  private var loadFromCacheFile: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_today_item, this)
    actual_view?.setOnImageEventListener(this)
    actual_view?.maxScale = 64f
  }

  override fun onImageLoaded() {
    progress_wrapper?.makeGone()
  }

  override fun onReady() {
    actual_view?.apply {
      val ratio = sWidth.toFloat() / sHeight.toFloat()
      var s = if (ratio > screenWidth / screenHeight) {
        screenHeight / (screenWidth / ratio)
      } else {
        screenWidth / (screenHeight * ratio)
      }
      s *= this.minScale// 手动将图片填满屏幕，再将图片置为 SCALE_TYPE_START
      animateScaleAndCenter(s, viewToSourceCoord(0f, 0f))?.withOnAnimationEventListener(object : SubsamplingScaleImageView.OnAnimationEventListener {
        override fun onInterruptedByUser() {
          setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
        }

        override fun onInterruptedByNewAnim() {
          setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
        }

        override fun onComplete() {
          setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
        }

      })?.start()
    }
  }

  override fun onTileLoadError(e: Exception?) {
    showMsg(e?.message)
    progress_wrapper?.makeGone()
  }

  override fun onPreviewReleased() = Unit

  override fun onImageLoadError(e: Exception?) {
    showMsg(e?.message)
    progress_wrapper?.makeGone()
  }

  override fun onPreviewLoadError(e: Exception?) {
    showMsg(e?.message)
    progress_wrapper?.makeGone()
  }

  fun updateUrl(url: String) {
    progress_wrapper?.makeVisible()
    if (this.tag != url) {
      actual_view?.recycle()
      actual_view?.invalidate()
      this.tag = url
    }
    loadActual(url)
  }

  private fun loadActual(url: String) {
    context.runOnUiThread {
      Glide.with(context).load(url).into(object : CustomTarget<Drawable>() {
        override fun onLoadCleared(placeholder: Drawable?) {}
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
          actual_view.setImage(ImageSource.bitmap(resource.toBitmap()))
          onImageDownloaded?.invoke()
        }
      })
    }
  }

  private fun showMsg(msg: String?) {
    context.toast(msg ?: "")
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    hasAttachedWindow = true
  }

  override fun onDetachedFromWindow() {
    hasAttachedWindow = false
    removeCallbacks(loadFromCacheFile)
    super.onDetachedFromWindow()
  }
}
