package io.nichijou.tujian.common.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class PostResp(
  @Json(name = "data")
  val data: Post,
  @Json(name = "code")
  val code: Int, // 200
  @Json(name = "msg")
  val msg: String, // 恭喜您，投稿成功！
  @Json(name = "tgResponse")
  val tgResponse: Boolean // true
)


