package io.nichijou.tujian.common.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BingResp(
  @Json(name = "images")
  val data: List<Bing>
)
