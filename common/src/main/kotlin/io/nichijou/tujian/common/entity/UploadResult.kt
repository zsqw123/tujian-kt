package io.nichijou.tujian.common.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class UploadResult(
  @Json(name = "info")
  val info: Info? = null,
  @Json(name = "error")
  val error: Error? = null,
  @Json(name = "ret")
  val ret: Boolean = false // true
) : Parcelable {
  @Parcelize
  @JsonClass(generateAdapter = true)
  data class Info(
    @Json(name = "md5")
    val md5: String, // 04b1fdbbf3ee2b4c03296489e8ecb47d
    @Json(name = "size")
    val size: Int // 1377234
  ) : Parcelable

  @Parcelize
  @JsonClass(generateAdapter = true)
  data class Error(
    @Json(name = "code")
    val code: Int, // 2
    @Json(name = "message")
    val message: String // Request method error.
  ) : Parcelable
}
