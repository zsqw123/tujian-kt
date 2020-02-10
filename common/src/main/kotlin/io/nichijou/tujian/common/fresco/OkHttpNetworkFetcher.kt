package io.nichijou.tujian.common.fresco

import android.os.SystemClock
import com.facebook.imagepipeline.common.BytesRange
import com.facebook.imagepipeline.image.EncodedImage
import com.facebook.imagepipeline.producers.*
import io.nichijou.tujian.common.ext.await
import io.nichijou.tujian.common.ext.loge
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Request
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class OkHttpNetworkFetcher(private val mCallFactory: Call.Factory) : BaseNetworkFetcher<OkHttpNetworkFetcher.OkHttpNetworkFetchState>() {
  override fun createFetchState(consumer: Consumer<EncodedImage>, context: ProducerContext): OkHttpNetworkFetchState {
    return OkHttpNetworkFetchState(consumer, context)
  }

  override fun fetch(fetchState: OkHttpNetworkFetchState, callback: NetworkFetcher.Callback) {
    try {
      fetchState.submitTime = SystemClock.elapsedRealtime()
      val requestBuilder = Request.Builder()
        .url(fetchState.uri.toString())
        .get()
      val bytesRange = fetchState.context.imageRequest.bytesRange
      if (bytesRange != null) {
        requestBuilder.addHeader("Range", bytesRange.toHttpRangeHeaderValue())
      }
      HEADERS[fetchState.uri.toString()]?.forEach {
        requestBuilder.addHeader(it.key, it.value)
      }
      fetchWithRequest(fetchState, callback, requestBuilder.build())
    } catch (e: Exception) {
      callback.onFailure(e)
    }
  }

  override fun onFetchCompletion(fetchState: OkHttpNetworkFetchState, byteSize: Int) {
    fetchState.fetchCompleteTime = SystemClock.elapsedRealtime()
  }

  override fun getExtraMap(fetchState: OkHttpNetworkFetchState, byteSize: Int): Map<String, String>? {
    val extraMap = HashMap<String, String>(4)
    extraMap[QUEUE_TIME] = (fetchState.responseTime - fetchState.submitTime).toString()
    extraMap[FETCH_TIME] = (fetchState.fetchCompleteTime - fetchState.responseTime).toString()
    extraMap[TOTAL_TIME] = (fetchState.fetchCompleteTime - fetchState.submitTime).toString()
    extraMap[IMAGE_SIZE] = byteSize.toString()
    return extraMap
  }

  private fun fetchWithRequest(
    fetchState: OkHttpNetworkFetchState,
    callback: NetworkFetcher.Callback,
    request: Request
  ) {
    val job = GlobalScope.launch(context = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
      loge("fresco load error", throwable)
      callback.onFailure(throwable)
    }) {
      val response = mCallFactory.newCall(request).await()
      fetchState.responseTime = SystemClock.elapsedRealtime()
      response.body.use { body ->
        if (response.isSuccessful && body != null) {
          val responseRange = BytesRange.fromContentRangeHeader(response.header("Content-Range"))
          if (responseRange != null && !(responseRange.from == 0 && responseRange.to == BytesRange.TO_END_OF_CONTENT)) {
            fetchState.responseBytesRange = responseRange
            fetchState.onNewResultStatusFlags = Consumer.IS_PARTIAL_RESULT
          }
          var contentLength = body.contentLength()
          if (contentLength < 0) {
            contentLength = 0
          }
          callback.onResponse(body.byteStream(), contentLength.toInt())
        } else {
          if (isActive) {
            callback.onFailure(IOException("Unexpected HTTP code $response"))
          } else {
            callback.onCancellation()
          }
        }
      }
    }
    fetchState.context.addCallbacks(
      object : BaseProducerContextCallbacks() {
        override fun onCancellationRequested() {
          job.cancel()
        }
      })
  }

  class OkHttpNetworkFetchState(consumer: Consumer<EncodedImage>, producerContext: ProducerContext) :
    FetchState(consumer, producerContext) {
    var submitTime: Long = 0
    var responseTime: Long = 0
    var fetchCompleteTime: Long = 0
  }

  companion object {
    val HEADERS = WeakHashMap<String, HashMap<String, String>?>()
    private const val QUEUE_TIME = "queue_time"
    private const val FETCH_TIME = "fetch_time"
    private const val TOTAL_TIME = "total_time"
    private const val IMAGE_SIZE = "image_size"
  }
}
