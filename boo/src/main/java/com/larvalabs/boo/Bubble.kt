package com.larvalabs.boo

import android.graphics.*

class Bubble {

  private val rotationPeriods: IntArray = IntArray(8)

  private val path: Path

  private val testPaint: Paint

  init {
    for (i in 0..7) {
      rotationPeriods[i] = MathUtils.random(MIN_PERIOD, MAX_PERIOD)
    }
    path = Path()
    testPaint = Paint()
    testPaint.isAntiAlias = true
  }

  fun draw(canvas: Canvas, paint: Paint, size: Float, t: Long) {
    path.reset()
    var iTheta: Float
    var jTheta: Float
    var iX: Float
    var iY: Float
    var jX: Float
    var jY: Float
    for (i in 0..7) {
      val j = (i + 1) % 8
      iTheta = MathUtils.cycle(t, rotationPeriods[i].toFloat()) * MathUtils.TWO_PI
      jTheta = MathUtils.cycle(t, rotationPeriods[j].toFloat()) * MathUtils.TWO_PI
      iX = (Math.cos(iTheta.toDouble()) * size.toDouble() * WOBBLE_AMOUNT.toDouble()).toFloat()
      iY = (Math.sin(iTheta.toDouble()) * size.toDouble() * WOBBLE_AMOUNT.toDouble()).toFloat()
      jX = (Math.cos(jTheta.toDouble()) * size.toDouble() * WOBBLE_AMOUNT.toDouble()).toFloat()
      jY = (Math.sin(jTheta.toDouble()) * size.toDouble() * WOBBLE_AMOUNT.toDouble()).toFloat()
      if (i == 0) {
        path.moveTo(CIRCLE_8_X[i] * size + iX, CIRCLE_8_Y[i] * size + iY)
      }
      path.cubicTo(CIRCLE_8_BX[i] * size + iX, CIRCLE_8_BY[i] * size + iY, CIRCLE_8_AX[j] * size + jX, CIRCLE_8_AY[j] * size + jY, CIRCLE_8_X[j] * size + jX, CIRCLE_8_Y[j] * size + jY)
    }
    path.close()
    canvas.drawPath(path, paint)
  }

  companion object {

    private val SPLINE_8 = 0.2652031f
    private val SQRT_HALF = Math.sqrt(0.5).toFloat()

    private val CIRCLE_8_X = floatArrayOf(0f, SQRT_HALF, 1f, SQRT_HALF, 0f, -SQRT_HALF, -1f, -SQRT_HALF)

    private val CIRCLE_8_Y = floatArrayOf(1f, SQRT_HALF, 0f, -SQRT_HALF, -1f, -SQRT_HALF, 0f, SQRT_HALF)

    private val CIRCLE_8_THETA = floatArrayOf(0f, -MathUtils.PI_OVER_4, -MathUtils.PI_OVER_2, -3 * MathUtils.PI_OVER_4, MathUtils.PI, 3 * MathUtils.PI_OVER_4, MathUtils.PI_OVER_2, MathUtils.PI_OVER_4)

    private val CIRCLE_8_AX = FloatArray(8)
    private val CIRCLE_8_AY = FloatArray(8)
    private val CIRCLE_8_BX = FloatArray(8)
    private val CIRCLE_8_BY = FloatArray(8)

    private val TEST_COLORS = IntArray(8)

    init {
      val hsv = FloatArray(3)
      hsv[1] = 1f
      hsv[2] = 1f
      for (i in 0..7) {
        CIRCLE_8_AX[i] = (CIRCLE_8_X[i] - Math.cos(CIRCLE_8_THETA[i].toDouble()) * SPLINE_8).toFloat()
        CIRCLE_8_BX[i] = (CIRCLE_8_X[i] + Math.cos(CIRCLE_8_THETA[i].toDouble()) * SPLINE_8).toFloat()
        CIRCLE_8_AY[i] = (CIRCLE_8_Y[i] - Math.sin(CIRCLE_8_THETA[i].toDouble()) * SPLINE_8).toFloat()
        CIRCLE_8_BY[i] = (CIRCLE_8_Y[i] + Math.sin(CIRCLE_8_THETA[i].toDouble()) * SPLINE_8).toFloat()
        hsv[0] = i * 360 / 8f
        TEST_COLORS[i] = -0x7f000001 and Color.HSVToColor(hsv)
      }
    }

    private val MIN_PERIOD = 1500
    private val MAX_PERIOD = 2500

    private val WOBBLE_AMOUNT = 0.035f
  }

}
