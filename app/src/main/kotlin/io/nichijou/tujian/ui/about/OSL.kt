package io.nichijou.tujian.ui.about

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OSL(
  @Json(name = "desc")
  val desc: String,
  @Json(name = "license")
  val license: String,
  @Json(name = "licenseUrl")
  val licenseUrl: String,
  @Json(name = "name")
  val name: String,
  @Json(name = "website")
  val website: String
)
