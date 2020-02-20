package io.nichijou.tujian.common.entity

data class Upload(
  var title: CharSequence,
  var content: CharSequence,
  var url: CharSequence,
  var user: CharSequence,
  var sort: CharSequence,
  var hz: CharSequence?
)
