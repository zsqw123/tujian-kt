package io.nichijou.tujian.ui.upload

import android.app.*
import android.net.*
import androidx.lifecycle.*
import io.nichijou.tujian.R
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.BuildConfig
import io.nichijou.tujian.common.entity.*
import io.nichijou.tujian.common.ext.*
import kotlinx.coroutines.*
import okhttp3.*
import java.net.*
import java.util.regex.*


class UploadViewModel(application: Application, private val tujianService: TujianService) : AndroidViewModel(application) {
  val msg = MutableLiveData<String>()
  val url = MutableLiveData<String>()
  val result = MutableLiveData<Post>()

  fun upload(uri: Uri) {
    viewModelScope.launch(Dispatchers.IO) {
      val requestBody = UriRequestBody(getApplication(), uri)
      val contentType = requestBody.contentType()?.toString() ?: "image/jpeg"
      val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
      if (inputStream == null) {
        msg.postValue(getApplication<Application>().getString(R.string.cant_resolve_selected_picture))
        return@launch
      }
      SimpleUpload(URL(BuildConfig.API_UPLOAD))
        .addFilePart("file", inputStream, "upload.$contentType", contentType)
        .upload(object : SimpleUpload.OnFileUploadedListener {
          override fun onFileUploadingSuccess(response: String) {
            logd("resp: $response, $contentType")
            val matcher = Pattern.compile("<h1>MD5:\\s([a-z0-9]*)</h1>").matcher(response)
            if (matcher.find()) {
              val md5 = matcher.group(1)
              if (md5.isNullOrBlank()) {
                msg.postValue(getApplication<Application>().getString(R.string.resolve_upload_result_is_null))
              } else {
                url.postValue(BuildConfig.API_PREFIX + md5)
              }
            } else {
              msg.postValue(getApplication<Application>().getString(R.string.resolve_upload_result_is_null))
            }
          }

          override fun onFileUploadingFailed(responseCode: Int) {
            msg.postValue(getApplication<Application>().getString(R.string.upload_error))
          }
        })
    }
  }

  fun post(upload: Upload) {
    viewModelScope.launch(Dispatchers.IO) {
      val builder = FormBody.Builder()
      val clazz = upload::class.java
      for (field in clazz.declaredFields) {
        field.isAccessible = true
        builder.add(field.name, field.get(upload).toString())
      }
      val response = tujianService.post(formBody = builder.build())
      val body = response.body()
      if (response.isSuccessful && body != null) {
        result.postValue(body.data)
      } else {
        msg.postValue(getApplication<Application>().getString(R.string.post_error))
      }
    }
  }
}
