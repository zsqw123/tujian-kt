package io.nichijou.tujian.common.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 分类归档结果
@JsonClass(generateAdapter = true)
data class ListResp(
  @Json(name = "maxpage")
  val max: Int, // 8
  @Json(name = "result")
  val data: List<Picture>
)
