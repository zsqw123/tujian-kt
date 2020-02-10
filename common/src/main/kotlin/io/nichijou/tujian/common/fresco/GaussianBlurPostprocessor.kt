package io.nichijou.tujian.common.fresco

import android.content.Context
import com.facebook.cache.common.CacheKey
import com.facebook.cache.common.SimpleCacheKey
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter
import jp.wasabeef.fresco.processors.gpu.GPUFilterPostprocessor

class GaussianBlurPostprocessor @JvmOverloads constructor(context: Context, private val blurSize: Float = 10f) : GPUFilterPostprocessor(context, GPUImageGaussianBlurFilter()) {

  init {
    getFilter<GPUImageGaussianBlurFilter>().setBlurSize(blurSize)
  }

  override fun getPostprocessorCacheKey(): CacheKey? {
    return SimpleCacheKey("gaussian_blur=$blurSize")
  }
}
