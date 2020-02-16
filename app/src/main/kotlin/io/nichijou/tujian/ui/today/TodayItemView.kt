package io.nichijou.tujian.ui.today

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.Priority
import com.facebook.imagepipeline.request.ImageRequest
import io.nichijou.tujian.R
import io.nichijou.tujian.common.ext.getScreenHeight
import io.nichijou.tujian.common.ext.getScreenWidth
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import io.nichijou.tujian.common.fresco.getFileFromDiskCache
import kotlinx.android.synthetic.main.view_today_item.view.*
import org.jetbrains.anko.toast

class TodayItemView(context: Context) : FrameLayout(context), SubsamplingScaleImageView.OnImageEventListener {

  private var onImageDownloaded: (() -> Unit)? = null
  private var downloaded = false
  private var dataSource: DataSource<Void>? = null
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
          setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
        }

        override fun onInterruptedByNewAnim() {
          setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
        }

        override fun onComplete() {
          setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
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
    progress?.text = "0"
    if (this.tag != url) {
      actual_view?.recycle()
      actual_view?.invalidate()
      this.tag = url
    }
    loadActual(url)
  }

  private fun loadActual(url: String) {
    downloaded = false
    val request = ImageRequest.fromUri(Uri.parse(url)) ?: return
    val cached = request.getFileFromDiskCache()
    if (cached != null && cached.exists()) {
      actual_view?.setImage(ImageSource.uri(Uri.fromFile(cached)))
      downloaded = true
      onImageDownloaded?.invoke()
      return
    }
    val pipeline = Fresco.getImagePipeline()
    dataSource = pipeline.prefetchToDiskCache(request, null, Priority.HIGH)
    dataSource?.subscribe(object : BaseDataSubscriber<Void>() {
      override fun onProgressUpdate(dataSource: DataSource<Void>) {
        if (hasAttachedWindow) {
          progress?.text = (dataSource.progress * 100).toInt().toString()
        }
      }

      override fun onFailureImpl(dataSource: DataSource<Void>) {
        showMsg(dataSource.failureCause?.message)
        if (hasAttachedWindow) {
          progress_wrapper?.makeGone()
        }
      }

      override fun onNewResultImpl(dataSource: DataSource<Void>) {
        if (dataSource.isFinished) {
          loadFromCacheFile = {
            val file = ImageRequest.fromUri(Uri.parse(url))?.getFileFromDiskCache()
            if (file != null && file.exists() && hasAttachedWindow) {
              actual_view?.setImage(ImageSource.uri(Uri.fromFile(file)))
              downloaded = true
              onImageDownloaded?.invoke()
            } else {
              progress_wrapper?.makeGone()
              showMsg("file have not been loaded yet.")
            }
          }
          postDelayed(loadFromCacheFile, 360)
        }
      }
    }, UiThreadImmediateExecutorService.getInstance())
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
    dataSource?.close()
    super.onDetachedFromWindow()
  }
}
