package io.nichijou.tujian.common.entity

import com.squareup.moshi.Json

abstract class BaseEntity(@Json(name = "ignore_updated") open var updated: Long = System.currentTimeMillis())
