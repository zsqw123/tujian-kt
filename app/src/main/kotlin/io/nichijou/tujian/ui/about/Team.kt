package io.nichijou.tujian.ui.about

import com.squareup.moshi.*


@JsonClass(generateAdapter = true)
data class Team(
  @Json(name = "avatar")
  val avatar: String,
  @Json(name = "job")
  val job: String, // 小程序
  @Json(name = "name")
  val name: String, // Delsart
  @Json(name = "type")
  val type: Int, // 2
  @Json(name = "url")
  val url: String
)
