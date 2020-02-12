package io.nichijou.tujian.ui

import android.net.*
import android.os.*
import com.davemorrissey.labs.subscaleview.*
import com.facebook.common.executors.*
import com.facebook.datasource.*
import com.facebook.drawee.backends.pipeline.*
import com.facebook.imagepipeline.common.*
import com.facebook.imagepipeline.request.*
import io.nichijou.tujian.R
import io.nichijou.tujian.base.*
import io.nichijou.tujian.common.fresco.*
import kotlinx.android.synthetic.main.activity_wallpaper.*

class WallpaperActivity : BaseActivity() {

  override fun isFullScreen(): Boolean = true

  override fun getContentViewId(): Int = R.layout.activity_wallpaper

  private val url = "https://files.yande.re/image/3ec451016ac9e44725cc258ed3a478ed/yande.re%20558669%20amano_hina%20erect_nipples%20niko_%28tama%29%20no_bra%20nopan%20see_through%20tenki_no_ko%20wet%20wet_clothes.jpg"

  override fun handleOnCreate(savedInstanceState: Bundle?) {
    initView()
    loadActual(url)
  }

  private var dataSource: DataSource<Void>? = null
  private var loadFromFile: (() -> Unit)? = null
  private fun initView() {
    top_layer.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
    top_layer.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
      override fun onImageLoaded() {
        top_layer?.apply {
          setScaleAndCenter(minScale, center)
        }
      }
    })
    bg_layer.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
    bg_layer.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
      override fun onImageLoaded() {
        bg_layer?.apply {
          setScaleAndCenter(minScale, center)
        }
      }
    })
  }

  private fun loadActual(url: String) {
    val request = ImageRequest.fromUri(Uri.parse(url)) ?: return
    val cached = request.getFileFromDiskCache()
    if (cached != null && cached.exists()) {
      top_layer?.setImage(ImageSource.uri(Uri.fromFile(cached)))
      bg_layer?.setImage(ImageSource.uri(Uri.fromFile(cached)))
      return
    }
    val pipeline = Fresco.getImagePipeline()
    dataSource = pipeline.prefetchToDiskCache(request, null, Priority.HIGH)
    dataSource?.subscribe(object : BaseDataSubscriber<Void>() {
      override fun onProgressUpdate(dataSource: DataSource<Void>) {
      }

      override fun onFailureImpl(dataSource: DataSource<Void>) {
      }

      override fun onNewResultImpl(dataSource: DataSource<Void>) {
        if (dataSource.isFinished) {
          loadFromFile = {
            val file = ImageRequest.fromUri(Uri.parse(url))?.getFileFromDiskCache()
            if (file != null && file.exists()) {
              top_layer?.setImage(ImageSource.uri(Uri.fromFile(file)))
              bg_layer?.setImage(ImageSource.uri(Uri.fromFile(file)))
            } else {
            }
          }
          top_layer?.postDelayed(loadFromFile, 360)
        }
      }
    }, UiThreadImmediateExecutorService.getInstance())
  }
}

