package io.nichijou.tujian.common.ext

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.NonNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.BufferedSink
import okio.IOException
import okio.source
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
suspend fun Call.await(): Response {

  return suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        continuation.resume(response) {
          continuation.resumeWithException(it)
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        if (continuation.isCancelled) return
        continuation.resumeWithException(e)
      }
    })

    continuation.invokeOnCancellation {
      try {
        cancel()
      } catch (ex: Throwable) {
        //Ignore cancel exception
      }
    }
  }
}

class UriRequestBody(private val context: Context, private val uri: Uri) : RequestBody() {

  override fun contentType(): MediaType? = (context.contentResolver.getType(uri)
    ?: "image/*").toMediaTypeOrNull()

  override fun contentLength(): Long {
    return context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null)
      ?.use {
        if (it.moveToFirst()) {
          it.getLong(0)
        } else {
          null
        }
      } ?: -1
  }

  override fun writeTo(@NonNull sink: BufferedSink) {
    context.contentResolver.openInputStream(uri)?.use {
      sink.writeAll(it.source())
    }
  }
}



