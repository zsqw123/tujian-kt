package io.nichijou.tujian.common.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryResp(
  @Json(name = "count")
  val count: Int, // 3
  @Json(name = "result")
  val data: List<Category>
)
