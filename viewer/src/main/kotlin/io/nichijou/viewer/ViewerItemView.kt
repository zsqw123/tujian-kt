package io.nichijou.viewer

import android.content.*
import android.net.*
import android.widget.*
import com.davemorrissey.labs.subscaleview.*
import com.facebook.common.executors.*
import com.facebook.datasource.*
import com.facebook.drawee.backends.pipeline.*
import com.facebook.imagepipeline.common.*
import com.facebook.imagepipeline.request.*
import io.nichijou.viewer.ext.*
import io.nichijou.viewer.internal.attachLongClick
import kotlinx.android.synthetic.main.view_viewer_item.view.*

class ViewerItemView(context: Context) : FrameLayout(context), SubsamplingScaleImageView.OnImageEventListener {
  private var onImageDownloaded: (() -> Unit)? = null
  private var downloaded = false
  private var dataSource: DataSource<Void>? = null
  private val screenWidth = context.getScreenWidth().toFloat()
  private val screenHeight = context.getScreenHeight().toFloat()
  private var loadFromCacheFile: (() -> Unit)? = null

  private var autoFitScreen = false

  private var maxScale = 2f
    set(value) {
      field = value
      actual_view?.maxScale = maxScale
    }

  init {
    inflate(context, R.layout.view_viewer_item, this)
    actual_view?.setOnImageEventListener(this)
    actual_view?.maxScale = maxScale
    actual_view.attachLongClick(context)
  }

  override fun onImageLoaded() {
    progress_wrapper?.makeGone()
    thumb_view?.makeGone()
  }

  override fun onReady() {
    if (autoFitScreen) {
      actual_view?.apply {
        val ratio = sWidth.toFloat() / sHeight.toFloat()
        val scale = if (ratio > screenWidth / screenHeight) {
          screenHeight / (screenWidth / ratio)
        } else {
          screenWidth / (screenHeight * ratio)
        }
        animateScaleAndCenter(scale * this.minScale, viewToSourceCoord(0f, 0f))?.start()
      }
    }
  }

  override fun onTileLoadError(e: Exception?) {
    msg(e?.message)
    progress_wrapper?.makeGone()
  }

  override fun onPreviewReleased() {
  }

  override fun onImageLoadError(e: Exception?) {
    msg(e?.message)
    progress_wrapper?.makeGone()
  }

  override fun onPreviewLoadError(e: Exception?) {
    msg(e?.message)
    progress_wrapper?.makeGone()
  }

  fun getActualImageView(): SubsamplingScaleImageView? = actual_view

  fun update(url: String, thumb: String? = null) {
    progress_wrapper?.makeVisible()
    thumb_view?.makeVisible()
    progress?.text = "0"
    val tag = thumb + url
    if (this.tag != tag) {
      actual_view?.recycle()
      actual_view?.invalidate()
      this.tag = tag
    }
    loadThumb(thumb ?: url)
    loadActual(url)
  }

  private fun loadThumb(url: String) {
    val uri = Uri.parse(url)
    thumb_view?.apply {
      val requestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
        .setRotationOptions(RotationOptions.autoRotate())
        .setProgressiveRenderingEnabled(true)
      val controllerBuilder = Fresco.newDraweeControllerBuilder()
        .setAutoPlayAnimations(true)
        .setImageRequest(requestBuilder.build())
        .setOldController(this.controller)
        .setAutoPlayAnimations(true)
        .setTapToRetryEnabled(true)
        .setRetainImageOnFailure(true)
      this.controller = controllerBuilder.build()
    }
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
        progress?.text = (dataSource.progress * 100).toInt().toString()
      }

      override fun onFailureImpl(dataSource: DataSource<Void>) {
        msg(dataSource.failureCause?.message)
        progress_wrapper?.makeGone()
      }

      override fun onNewResultImpl(dataSource: DataSource<Void>) {
        if (dataSource.isFinished) {
          loadFromCacheFile = {
            val file = ImageRequest.fromUri(Uri.parse(url))?.getFileFromDiskCache()
            if (file != null && file.exists()) {
              actual_view?.setImage(ImageSource.uri(Uri.fromFile(file)))
              downloaded = true
              onImageDownloaded?.invoke()
            } else {
              progress_wrapper?.makeGone()
              msg("file have not been loaded yet.")
            }
          }
          postDelayed(loadFromCacheFile, 360)
        }
      }
    }, UiThreadImmediateExecutorService.getInstance())
  }

  private fun msg(msg: String?) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
  }

  override fun onDetachedFromWindow() {
    removeCallbacks(loadFromCacheFile)
    dataSource?.safeClose()
    super.onDetachedFromWindow()
  }
}
