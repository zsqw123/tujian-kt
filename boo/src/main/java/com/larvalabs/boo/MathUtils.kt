package com.larvalabs.boo

import android.graphics.Matrix
import android.view.animation.Interpolator
import java.util.*
import kotlin.math.*

object MathUtils {

  val SQRT_2 = sqrt(2.0).toFloat()
  const val TWO_PI = (Math.PI * 2).toFloat()
  const val PI = Math.PI.toFloat()
  const val PI_OVER_2 = (Math.PI / 2.0).toFloat()
  const val PI_OVER_4 = (Math.PI / 4.0).toFloat()

  val ROOT_PI_OVER_TWO = sqrt(Math.PI / 2).toFloat()

  val RANDOM = Random()

  private val srcTriangle = FloatArray(6)
  private val dstTriangle = FloatArray(6)

  private val srcQuad = FloatArray(8)
  private val dstQuad = FloatArray(8)

  private val work = Point()

  fun clamp(x: Float, min: Float, max: Float): Float {
    var x = x
    if (x < min) {
      x = min
    } else if (x > max) {
      x = max
    }
    return x
  }

  fun degreesToRadians(degrees: Float): Float {
    return degrees * PI / 180
  }

  fun radiansToDegrees(degrees: Float): Float {
    return 180 * degrees / PI
  }

  fun map(x: Float, a: Float, b: Float, u: Float, v: Float): Float {
    val p = (x - a) / (b - a)
    return u + p * (v - u)
  }

  fun mapLong(x: Double, a: Double, b: Double, u: Long, v: Long): Long {
    val p = (x - a) / (b - a)
    return (u + p * (v - u)).toLong()
  }

  fun mapInt(x: Float, a: Float, b: Float, u: Int, v: Int): Int {
    val p = (x - a) / (b - a)
    return (u + p * (v - u)).toInt()
  }

  fun mapInt(x: Double, a: Double, b: Double, u: Int, v: Int): Int {
    val p = (x - a) / (b - a)
    return (u + p * (v - u)).toInt()
  }

  fun mapInt(x: Float, a: Float, b: Float, u: Int, v: Int, interpolator: Interpolator): Int {
    val p = interpolator.getInterpolation((x - a) / (b - a))
    return (u + p * (v - u)).toInt()
  }

  fun clampedMapInt(x: Float, a: Float, b: Float, u: Int, v: Int): Int {
    if (x < a) {
      return u
    } else if (x > b) {
      return v
    }
    val p = (x - a) / (b - a)
    return (u + p * (v - u)).toInt()
  }

  fun clampedMapInt(x: Double, a: Double, b: Double, u: Int, v: Int): Int {
    if (x < a) {
      return u
    } else if (x > b) {
      return v
    }
    val p = (x - a) / (b - a)
    return (u + p * (v - u)).toInt()
  }

  fun clampedMap(x: Float, a: Float, b: Float, u: Float, v: Float): Float {
    if (x <= a) {
      return u
    } else if (x >= b) {
      return v
    }
    val p = (x - a) / (b - a)
    return u + p * (v - u)
  }

  fun clampedMap(x: Double, a: Double, b: Double, u: Double, v: Double): Double {
    if (x <= a) {
      return u
    } else if (x >= b) {
      return v
    }
    val p = (x - a) / (b - a)
    return u + p * (v - u)
  }

  fun clampedMap(x: Float, a: Float, b: Float, u: Float, v: Float, interpolator: Interpolator): Float {
    if (x <= a) {
      return u
    } else if (x >= b) {
      return v
    }
    val p = interpolator.getInterpolation((x - a) / (b - a))
    return u + p * (v - u)
  }

  fun map(x: Float, a: Float, b: Float, u: Float, v: Float, interpolator: Interpolator): Float {
    val p = interpolator.getInterpolation((x - a) / (b - a))
    return u + p * (v - u)
  }

  fun map(x: Double, a: Double, b: Float, u: Float, v: Float, interpolator: Interpolator): Float {
    val p = interpolator.getInterpolation(((x - a) / (b - a)).toFloat())
    return u + p * (v - u)
  }

  fun cycle(time: Long, period: Float): Float {
    val v = time % period
    return map(v, 0f, period, 0f, 1f)
  }

  fun cycle(time: Long, period: Float, phaseShift: Float): Float {
    var v = (time.toDouble() + phaseShift * period) % period
    if (v < 0) {
      v += period.toDouble()
    }
    return map(v.toFloat(), 0f, period, 0f, 1f)
  }

  // Go from 0 to 1 and back down again, but "hanging" out at 1 for a while.
  fun hang(time: Long, period: Float): Float {
    val v = time % period * 2f * ROOT_PI_OVER_TWO / period - ROOT_PI_OVER_TWO
    return cos((v * v).toDouble()).toFloat()
  }

  // Smoothly oscillate from 0 to 1 and back
  fun smoothPulse(time: Long, period: Float): Float {
    val v = clampedMap(time.toDouble(), 0.0, period.toDouble(), (-PI).toDouble(), PI.toDouble())
    return (cos(v) + 1).toFloat() / 2
  }

  fun cycleBackwards(time: Long, period: Float): Float {
    return 1 - cycle(time, period)
  }

  fun oscillate(time: Long, period: Float): Float {
    return sin(time.toDouble() * TWO_PI / period.toDouble()).toFloat()
  }

  fun doubleOscillate(time: Long, period1: Float, period2: Float): Float {
    return ((sin(time.toDouble() * TWO_PI / period1.toDouble()) + sin(time.toDouble() * TWO_PI / period2.toDouble())) / 2).toFloat()
  }

  // Phase shift is 1-based
  fun oscillate(time: Long, period: Float, phaseShift: Float): Float {
    return sin((time / period.toDouble() + phaseShift) * TWO_PI).toFloat()
  }

  // Do a smooth pulse at start of a longer period
  fun pulse(time: Long, period: Float, pulseWidth: Float, phaseShift: Float): Float {
    val t = cycle(time, period, phaseShift) * period / pulseWidth
    return if (t >= 1) {
      0f
    } else {
      map(cos((t * TWO_PI).toDouble()).toFloat(), -1f, 1f, 1f, 0f)
    }
  }

  fun fractionalPart(v: Float): Float {
    return (v - floor(v.toDouble())).toFloat()
  }

  fun quadrant(time: Long, period: Float): Int {
    val v = time * TWO_PI / period
    val x = cos(v.toDouble()).toFloat()
    val y = sin(v.toDouble()).toFloat()
    return if (x > 0 && y > 0) {
      0
    } else if (x < 0 && y > 0) {
      1
    } else if (x < 0 && y < 0) {
      2
    } else {
      3
    }
  }

  fun phase(time: Long, period: Float, n: Int): Int {
    val v = cycle(time, period)
    return (v * n).toInt()
  }

  fun random(min: Float, max: Float): Float {
    return map(RANDOM.nextFloat(), 0f, 1f, min, max)
  }

  fun random(min: Int, max: Int): Int {
    var i = mapInt(RANDOM.nextFloat(), 0f, 1f, min, max + 1)
    if (i == max + 1) {
      i = max
    }
    return i
  }

  fun random(min: Long, max: Long): Long {
    var i = mapLong(RANDOM.nextDouble(), 0.0, 1.0, min, max + 1)
    if (i == max + 1) {
      i = max
    }
    return i
  }

  /**
   * Choose random numbers from 0 to n-1
   *
   * @param n       the choice limit.
   * @param results the array to put the choices. Infinite loop if length > n.
   */
  fun choose(n: Int, results: IntArray) {
    val k = results.size
    for (i in 0 until k) {
      var done = false
      while (!done) {
        results[i] = RANDOM.nextInt(n)
        done = true
        for (j in 0 until i) {
          if (results[j] == results[i]) {
            done = false
          }
        }
      }
    }
  }

  /**
   * Compute an overscroll-like soft clamping of a value between min and max.
   *
   * @param x
   * @param min
   * @param max
   * @param unit
   * @return
   */
  fun overshoot(x: Float, min: Float, max: Float, unit: Float): Float {
    return if (x > max) {
      val amount = (x - max) / unit
      val adjusted = amount / (amount + 1)
      adjusted * unit + max
    } else if (x < min) {
      val amount = (min - x) / unit
      val adjusted = amount / (amount + 1)
      min - adjusted * unit
    } else {
      // Still in range
      x
    }
  }

  // Get the transform that converts one triangle into another
  fun transformTriangle(
    x11: Float, y11: Float, x21: Float, y21: Float, x31: Float, y31: Float,
    x12: Float, y12: Float, x22: Float, y22: Float, x32: Float, y32: Float): Matrix {
    val matrix = Matrix()
    transformTriangle(x11, y11, x21, y21, x31, y31, x12, y12, x22, y22, x32, y32, matrix)
    return matrix
  }

  fun transformTriangle(x11: Float, y11: Float, x21: Float, y21: Float, x31: Float, y31: Float, x12: Float, y12: Float, x22: Float, y22: Float, x32: Float, y32: Float, matrix: Matrix) {
    srcTriangle[0] = x11
    srcTriangle[1] = y11
    srcTriangle[2] = x21
    srcTriangle[3] = y21
    srcTriangle[4] = x31
    srcTriangle[5] = y31
    dstTriangle[0] = x12
    dstTriangle[1] = y12
    dstTriangle[2] = x22
    dstTriangle[3] = y22
    dstTriangle[4] = x32
    dstTriangle[5] = y32
    matrix.setPolyToPoly(srcTriangle, 0, dstTriangle, 0, 3)
  }

  fun transformQuads(x11: Float, y11: Float, x21: Float, y21: Float, x31: Float, y31: Float, x41: Float, y41: Float, x12: Float, y12: Float, x22: Float, y22: Float, x32: Float, y32: Float, x42: Float, y42: Float, matrix: Matrix) {
    srcQuad[0] = x11
    srcQuad[1] = y11
    srcQuad[2] = x21
    srcQuad[3] = y21
    srcQuad[4] = x31
    srcQuad[5] = y31
    srcQuad[6] = x41
    srcQuad[7] = y41
    dstQuad[0] = x12
    dstQuad[1] = y12
    dstQuad[2] = x22
    dstQuad[3] = y22
    dstQuad[4] = x32
    dstQuad[5] = y32
    dstQuad[6] = x42
    dstQuad[7] = y42
    matrix.setPolyToPoly(srcQuad, 0, dstQuad, 0, 4)
  }

  fun applyToSphere(x: Float, y: Float, size: Float, radius: Float, curvature: Float, matrix: Matrix) {
    val x11 = x - size
    val y11 = y - size
    val x21 = x + size
    val y21 = y - size
    val x31 = x + size
    val y31 = y + size
    val x41 = x - size
    val y41 = y + size
    mapToSphere(x11, y11, radius, curvature, work)
    val x12 = work.x
    val y12 = work.y
    mapToSphere(x21, y21, radius, curvature, work)
    val x22 = work.x
    val y22 = work.y
    mapToSphere(x31, y31, radius, curvature, work)
    val x32 = work.x
    val y32 = work.y
    mapToSphere(x41, y41, radius, curvature, work)
    val x42 = work.x
    val y42 = work.y
    transformQuads(x11, y11, x21, y21, x31, y31, x41, y41, x12, y12, x22, y22, x32, y32, x42, y42, matrix)
  }

  private fun mapToSphere(x: Float, y: Float, radius: Float, curvature: Float, result: Point) {
    val theta = atan2(y.toDouble(), x.toDouble())
    val r = min(1.0, hypot(x.toDouble(), y.toDouble()) / radius)
    val newD = ((1 - curvature) * r + curvature * sqrt(r)) * radius
    result.x = (cos(theta) * newD).toFloat()
    result.y = (sin(theta) * newD).toFloat()
  }

  fun constrain(v: Int, min: Int, max: Int): Int {
    return min(max, max(v, min))
  }

  fun flip(chance: Float): Boolean {
    return RANDOM.nextFloat() < chance
  }

  fun <T> chooseAtRandom(list: List<T>): T {
    return list[RANDOM.nextInt(list.size)]
  }

}
