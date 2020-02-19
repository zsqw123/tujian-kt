package io.nichijou.tujian.common.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateResp(
  @Json(name = "version_code")
  val code: Int,
  @Json(name = "version_name")
  val name: String,
  @Json(name = "apk_url")
  val url: String,
  @Json(name = "update_log")
  val log: String,
  @Json(name = "update_time")
  val time: String
)


