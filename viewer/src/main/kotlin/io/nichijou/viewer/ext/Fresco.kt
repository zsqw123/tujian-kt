package io.nichijou.viewer.ext

import com.facebook.binaryresource.*
import com.facebook.datasource.*
import com.facebook.imagepipeline.cache.*
import com.facebook.imagepipeline.core.*
import com.facebook.imagepipeline.request.*
import java.io.*

fun ImageRequest.getFileFromDiskCache(): File? {
  val cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(this, null)
  if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
    val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
    return (resource as FileBinaryResource).file
  }
  return null
}

fun DataSource<*>?.safeClose() {
  if (this != null && !isClosed) close()
}
