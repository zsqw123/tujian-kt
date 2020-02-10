package io.nichijou.tujian.common.entity

data class Upload(
  val title: CharSequence,
  val content: CharSequence,
  val url: CharSequence,
  val user: CharSequence,
  val sort: CharSequence,
  val hz: CharSequence?
)
