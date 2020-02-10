package io.nichijou.tujian.common.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Post(
  @Json(name = "content")
  val content: String, // 幼なじみは絶対に負けない （© しぐれうい）
  @Json(name = "title")
  val title: String, // 幼なじみは絶対に負けない
  @Json(name = "url")
  val url: String, // https://img.dpic.dev/04b1fdbbf3ee2b4c03296489e8ecb47d
  @Json(name = "user")
  val user: String // iota9star
)
