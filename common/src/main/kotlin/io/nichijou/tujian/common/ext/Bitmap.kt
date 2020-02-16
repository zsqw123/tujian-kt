package io.nichijou.tujian.common.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

fun Bitmap.scale(destWidth: Int, destHeight: Int): Bitmap? {
  if (width == 0 || height == 0 ||
    destWidth == 0 || destHeight == 0) {
    return null
  }
  val largestDimension = max(destWidth, destHeight)
  var width = width
  var height = height
  when {
    width > height -> {
      // landscape
      val ratio = width.toFloat() / largestDimension
      width = largestDimension
      height = (height / ratio).toInt()
    }
    height > width -> {
      // portrait
      val ratio = height.toFloat() / largestDimension
      height = largestDimension
      width = (width / ratio).toInt()
    }
    else -> {
      height = largestDimension
      width = largestDimension
    }
  }
  return Bitmap.createScaledBitmap(this, width, height, true)
}


fun File.getBitmapSize(): Size {
  val options = BitmapFactory.Options()
  options.inJustDecodeBounds = true
  BitmapFactory.decodeFile(absolutePath, options)
  return Size(options.outWidth, options.outHeight)
}

fun String.getBitmapSize(): Size {
  val options = BitmapFactory.Options()
  options.inJustDecodeBounds = true
  BitmapFactory.decodeFile(this, options)
  return Size(options.outWidth, options.outHeight)
}

fun Size.fitScreenSize(context: Context): Size {
  val ratio = width / height.toFloat()
  return if (width > height) {
    val screenHeight = context.getScreenHeight()
    Size((screenHeight * ratio).toInt(), screenHeight)
  } else {
    val screenWidth = context.getScreenWidth()
    Size(screenWidth, (screenWidth / ratio).toInt())
  }
}

fun File.getFitScreenSizeBitmap(context: Context) {
  val screenHeight = context.getScreenHeight()
  val screenWidth = context.getScreenWidth()
  val options = BitmapFactory.Options()
  options.inJustDecodeBounds = true
  BitmapFactory.decodeFile(this.absolutePath, options)
  options.inJustDecodeBounds = false
  val ratio = options.outWidth / options.outHeight.toFloat()
  if (ratio > 1f) {
    val inSimpleSize = options.outWidth / screenWidth.toFloat()
    options.inSampleSize = if (inSimpleSize >= 1) inSimpleSize.roundToInt() else 1
    options.outWidth = (screenHeight * ratio).toInt()
    options.outHeight = screenHeight
  } else {
    val inSimpleSize = options.outHeight / screenHeight.toFloat()
    options.inSampleSize = if (inSimpleSize >= 1) inSimpleSize.roundToInt() else 1
    options.outWidth = screenWidth
    options.outHeight = (screenWidth / ratio).toInt()
  }
}
