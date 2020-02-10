package io.nichijou.tujian.common.fresco

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.palette.graphics.Palette
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.common.RotationOptions
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.facebook.imagepipeline.request.Postprocessor
import java.io.File

fun SimpleDraweeView.load(
  url: String?,
  headers: HashMap<String, String>? = null,
  radius: Float = 0f,
  @DrawableRes placeholderRes: Int = 0,
  @DrawableRes failureRes: Int = 0,
  width: Int = -1,
  height: Int = -1,
  fadeDuration: Int = 300,
  progressDrawable: Drawable? = null,
  postprocessor: Postprocessor? = null,
  autoPlayAnimations: Boolean = true,
  tapToRetryEnabled: Boolean = true,
  retainImageOnFailure: Boolean = true,
  progressiveRenderingEnabled: Boolean = true,
  actualImageScaleType: ScalingUtils.ScaleType? = null,
  placeholderImageScaleType: ScalingUtils.ScaleType? = null,
  failureImageScaleType: ScalingUtils.ScaleType? = null,
  loadedCallback: ((SimpleDraweeView, ImageInfo?) -> Unit)? = null
) {
  if (url.isNullOrBlank()) return
  val uri = Uri.parse(url) ?: return
  load(uri, headers, radius, placeholderRes, failureRes, width, height, fadeDuration, progressDrawable, postprocessor, autoPlayAnimations, tapToRetryEnabled, retainImageOnFailure, progressiveRenderingEnabled, actualImageScaleType, placeholderImageScaleType, failureImageScaleType, loadedCallback)
}

fun SimpleDraweeView.load(
  uri: Uri?,
  headers: HashMap<String, String>? = null,
  radius: Float = 0f,
  @DrawableRes placeholderRes: Int = 0,
  @DrawableRes failureRes: Int = 0,
  width: Int = -1,
  height: Int = -1,
  fadeDuration: Int = 300,
  progressDrawable: Drawable? = null,
  postprocessor: Postprocessor? = null,
  autoPlayAnimations: Boolean = true,
  tapToRetryEnabled: Boolean = true,
  retainImageOnFailure: Boolean = true,
  progressiveRenderingEnabled: Boolean = true,
  actualImageScaleType: ScalingUtils.ScaleType? = null,
  placeholderImageScaleType: ScalingUtils.ScaleType? = null,
  failureImageScaleType: ScalingUtils.ScaleType? = null,
  loadedCallback: ((SimpleDraweeView, ImageInfo?) -> Unit)? = null
) {
  if (uri == null) return
  if (headers != null) {
    OkHttpNetworkFetcher.HEADERS[uri.toString()] = headers
  }
  val hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(context.resources)
    .setFadeDuration(fadeDuration)
  if (actualImageScaleType != null) {
    hierarchyBuilder.actualImageScaleType = actualImageScaleType
  }
  if (placeholderImageScaleType != null) {
    hierarchyBuilder.placeholderImageScaleType = placeholderImageScaleType
  }
  if (failureImageScaleType != null) {
    hierarchyBuilder.failureImageScaleType = failureImageScaleType
  }
  if (progressDrawable != null) {
    hierarchyBuilder.progressBarImage = progressDrawable
  }
  if (placeholderRes != 0) {
    hierarchyBuilder.setPlaceholderImage(placeholderRes)
  }
  if (failureRes != 0) {
    hierarchyBuilder.setFailureImage(failureRes)
  }
  if (radius > 0) {
    hierarchyBuilder.roundingParams = RoundingParams.fromCornersRadius(radius)
  }
  this.hierarchy = hierarchyBuilder.build()
  val requestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
    .setRotationOptions(RotationOptions.autoRotate())
    .setProgressiveRenderingEnabled(progressiveRenderingEnabled)
  if (postprocessor != null) {
    requestBuilder.postprocessor = postprocessor
  }
  if (width > 0 && height > 0) {
    requestBuilder.resizeOptions = ResizeOptions(width, height)
  }
  val controllerBuilder = Fresco.newDraweeControllerBuilder()
    .setImageRequest(requestBuilder.build())
    .setOldController(this.controller)
    .setAutoPlayAnimations(autoPlayAnimations)
    .setTapToRetryEnabled(tapToRetryEnabled)
    .setRetainImageOnFailure(retainImageOnFailure)
  if (loadedCallback != null) {
    controllerBuilder.controllerListener = object : BaseControllerListener<ImageInfo>() {
      override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
        loadedCallback(this@load, imageInfo)
      }
    }
  }
  this.controller = controllerBuilder.build()
}

fun ImageRequest.getFileFromDiskCache(): File? {
  var localFile: File? = null
  val cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(this, null)
  if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
    val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
    localFile = (resource as FileBinaryResource).file
  }
  return localFile
}

fun ImageRequest.getPalette(callback: (Palette?) -> Unit): DataSource<CloseableReference<CloseableImage>>? {
  val source = Fresco.getImagePipeline().fetchDecodedImage(this, null)
  source.subscribe(object : BaseBitmapDataSubscriber() {
    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
    }

    override fun onNewResultImpl(bitmap: Bitmap?) {
      if (bitmap != null) {
        Palette.from(bitmap).generate {
          callback.invoke(it)
        }
      }
    }
  }, UiThreadImmediateExecutorService.getInstance())
  return source
}

fun String.getPaletteSwatches(callback: (String, List<Palette.Swatch>) -> Unit): DataSource<CloseableReference<CloseableImage>>? {
  val request = ImageRequest.fromUri(this) ?: return null
  val source = Fresco.getImagePipeline().fetchDecodedImage(request, null)
  source.subscribe(object : BaseBitmapDataSubscriber() {
    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
    }

    override fun onNewResultImpl(bitmap: Bitmap?) {
      if (bitmap != null) {
        Palette.from(bitmap.copy(Bitmap.Config.RGB_565, false)).generate {
          if (it != null) {
            callback.invoke(this@getPaletteSwatches, it.swatches)
          }
        }
      }
    }
  }, UiThreadImmediateExecutorService.getInstance())
  return source
}

fun ImageRequest.getPaletteSwatches(callback: (List<Palette.Swatch>) -> Unit): DataSource<CloseableReference<CloseableImage>>? {
  val source = Fresco.getImagePipeline().fetchDecodedImage(this, null)
  source.subscribe(object : BaseBitmapDataSubscriber() {
    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
    }

    override fun onNewResultImpl(bitmap: Bitmap?) {
      if (bitmap != null) {
        Palette.from(bitmap.copy(Bitmap.Config.RGB_565, false)).generate {
          if (it != null) {
            callback.invoke(it.swatches)
          }
        }
      }
    }
  }, UiThreadImmediateExecutorService.getInstance())
  return source
}

fun DataSource<*>?.stop() {
  if (this != null && !isClosed) close()
}
