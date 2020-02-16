package com.larvalabs.boo

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

class Eyes(private val creature: Creature, private val creatureInteraction: CreatureInteraction) {

  private val matrix = Matrix()
  private val workMatrix = Matrix()

  private val workRect = RectF()

  private var look: Behavior? = null
  private var blink: Behavior? = null
  private var squint: Behavior? = null
  private var notice: Behavior? = null
  private var freakOut: Behavior? = null
  private var stare: Behavior? = null

  private val testPaint: Paint = Paint()
  private var lookTarget: Creature? = null

  private enum class BehaviorType(internal var chance: Float, internal var changeTime: Long, internal var minDuration: Long, internal var maxDuration: Long) {

    BLINK(1f / 4f / 60f, 125, 75, 75),
    LOOK(1f / 3f / 60f, 300, 500, 3000),
    SQUINT(1f / 10f / 60f, 300, 1000, 4000),
    NOTICING(0f, 60, 0, 0),
    SCARED(0f, 100, 500, 600),
    STARE(0f, 20, 800, 2500);

    internal fun shouldStart(): Boolean {
      return MathUtils.flip(chance)
    }

  }

  private class Behavior internal constructor(internal var startTime: Long, type: BehaviorType, internal var param: Float) {
    internal var holdTime: Long = 0
    internal var stopTime: Long = 0
    internal var endTime: Long = 0

    init {
      val duration = MathUtils.random(type.minDuration, type.maxDuration)
      holdTime = startTime + type.changeTime
      stopTime = holdTime + duration
      endTime = stopTime + type.changeTime
    }

    internal fun isDone(t: Long): Boolean {
      return t >= endTime
    }

    internal fun getProgress(t: Long): Float {
      return if (t < holdTime) {
        MathUtils.map(t.toFloat(), startTime.toFloat(), holdTime.toFloat(), 0f, 1f, Util.PATH_CURVE)
      } else if (t < stopTime) {
        1f
      } else if (t < endTime) {
        MathUtils.map(t.toFloat(), stopTime.toFloat(), endTime.toFloat(), 1f, 0f, Util.PATH_CURVE)
      } else {
        0f
      }
    }

    internal fun cancel(t: Long) {
      var newEnd = endTime
      if (t < holdTime) {
        newEnd = t + (t - startTime)
      } else if (t < stopTime) {
        newEnd = t + (endTime - stopTime)
      }
      val delta = newEnd - endTime
      startTime += delta
      holdTime += delta
      stopTime += delta
      endTime += delta
    }

  }

  init {
    testPaint.isAntiAlias = true
    testPaint.color = -0x7fff0001
  }

  fun draw(canvas: Canvas, paint: Paint, bodySize: Float, eyeSize: Float, t: Long) {
    var cx = 0f
    var cy = 0f
    var eyeScale = 1f
    var curvature = 0f
    var blinkAmount = 0f
    var squintLevel = 0f
    if (look != null) {
      if (look!!.isDone(t)) {
        look = null
      } else {
        val p = look!!.getProgress(t)
        val theta: Float = if (lookTarget == null) {
          look!!.param
        } else {
          creature.getAngleTo(lookTarget!!)
        }
        cx = ((bodySize / 3).toDouble() * cos(theta.toDouble()) * p.toDouble()).toFloat()
        cy = ((bodySize / 2).toDouble() * sin(theta.toDouble()) * p.toDouble()).toFloat()
        curvature = MathUtils.map(p, 0f, 1f, 0f, EYE_CURVATURE)
      }
    } else {
      // Won't look around if scared
      if (notice == null && freakOut == null && stare == null && BehaviorType.LOOK.shouldStart()) {
        startLooking(t, null)
      }
    }
    if (notice != null) {
      if (notice!!.isDone(t)) {
        notice = null
        freakOut = Behavior(t, BehaviorType.SCARED, 0f)
      }
    } else if (freakOut != null) {
      if (freakOut!!.isDone(t)) {
        freakOut = null
      } else {
        val p = freakOut!!.getProgress(t)
        eyeScale = MathUtils.map(p, 0f, 1f, 1f, SCARED_SCALE)
      }
    }
    if (stare != null) {
      if (stare!!.isDone(t)) {
        stare = null
      }
    }
    if (blink != null) {
      if (blink!!.isDone(t)) {
        blink = null
      } else {
        blinkAmount = blink!!.getProgress(t)
      }
    } else {
      if (notice == null && freakOut == null && BehaviorType.BLINK.shouldStart()) {
        blink = Behavior(t, BehaviorType.BLINK, 0f)
      }
    }
    if (squint != null) {
      if (squint!!.isDone(t)) {
        squint = null
      } else {
        squintLevel = squint!!.getProgress(t)
      }
    } else {
      if (notice == null && freakOut == null && blink == null && BehaviorType.SQUINT.shouldStart()) {
        squint = Behavior(t, BehaviorType.SQUINT, 0f)
      }
    }
    if (squintLevel > 0) {
      blinkAmount = MathUtils.map(squintLevel, 0f, 1f, blinkAmount, 0.5f + blinkAmount / 2)
    }
    for (i in 0..1) {
      val x = if (i == 0) cx - bodySize / 3 else cx + bodySize / 3
      val y = cy
      canvas.save()
      matrix.reset()
      MathUtils.applyToSphere(x, y, eyeSize, bodySize, curvature, matrix)
      canvas.getMatrix(workMatrix)
      workMatrix.preConcat(matrix)
      canvas.setMatrix(workMatrix)
      canvas.scale(eyeScale, eyeScale, x, y)
      if (blinkAmount > 0) {
        val top = MathUtils.clampedMap(blinkAmount, 0f, 0.9f, y - eyeSize, y)
        val bottom = MathUtils.clampedMap(blinkAmount, 0f, 0.9f, y + eyeSize, y)
        workRect.set(x - 2 * eyeSize, top, x + 2 * eyeSize, bottom)
        canvas.clipRect(workRect)
        //                canvas.drawRect(workRect, testPaint);
      }
      if (blinkAmount < 0.9f) {
        canvas.drawCircle(x, y, eyeSize, paint)
      }
      canvas.restore()
    }

  }

  private fun startLooking(t: Long, target: Creature?) {
    val theta: Float
    if (creatureInteraction.isNewArrival(t) || MathUtils.flip(0.7f) || target != null) {
      lookTarget = target ?: creatureInteraction.getLookTarget(creature)
      theta = creature.getAngleTo(lookTarget!!)
    } else {
      lookTarget = null
      theta = MathUtils.random(0f, MathUtils.TWO_PI)
    }
    look = Behavior(t, BehaviorType.LOOK, theta)
  }

  fun getScared(t: Long) {
    if (look != null) {
      look!!.cancel(t)
    }
    if (stare != null) {
      stare!!.cancel(t)
    }
    if (blink != null) {
      blink!!.cancel(t)
    }
    if (squint != null) {
      squint!!.cancel(t)
    }
    notice = Behavior(t, BehaviorType.NOTICING, 0f)
  }

  fun comingBack(t: Long) {
    if (look != null) {
      look!!.cancel(t)
    }
    if (notice != null) {
      notice!!.cancel(t)
    }
    if (freakOut != null) {
      freakOut!!.cancel(t)
    }
    stare = Behavior(t, BehaviorType.STARE, 0f)
  }

  fun lookIfAble(t: Long, other: Creature) {
    if (notice == null && freakOut == null && look == null) {
      if (stare != null) {
        stare!!.cancel(t)
      }
      startLooking(t, other)
    }
  }

  companion object {

    private const val SCARED_SCALE = 1.65f
    private const val EYE_CURVATURE = 0.25f
  }

}
