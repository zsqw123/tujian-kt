package com.larvalabs.boo

import android.graphics.*

open class Creature(private val bodyColor: Int, private val eyeColor: Int, bodySize: Float, private val system: PhysicsSystem, private val index: Int, private val creatureInteraction: CreatureInteraction) {

  private var mode: Mode? = null
  private var behavior: Behavior? = null
  private val originalBodySize: Float
  var bodySize: Float = 0.toFloat()
    protected set
  private var eyeSize: Float = 0.toFloat()
  protected var paint: Paint

  private val bubble: Bubble
  private val eyes: Eyes

  private val startTime: Long

  private var scareTime: Long = -1

  private var escapeAngle: Float = 0.toFloat()

  private val position = Point(0f, 0f)

  private var comeBackTime: Long = 0

  private enum class Mode {

    OUT,
    RETURNING,
    IN,
    NOTICING,
    SCARED,
    ANTICIPATING,
    LEAVING;

    fun hiding(): Boolean {
      return this == OUT || this == LEAVING || this == SCARED || this == ANTICIPATING || this == NOTICING
    }

  }

  private enum class BehaviorType {
    MOVE,
    GROW
  }

  private class Behavior {

    internal var type: BehaviorType

    internal var startTime: Long = 0
    internal lateinit var position: Point
    internal var size: Float = 0.toFloat()

    private constructor(position: Point, t: Long) {
      this.position = position
      type = BehaviorType.MOVE
      startTime = t
    }

    internal constructor(size: Float, t: Long) {
      this.size = size
      type = BehaviorType.GROW
      startTime = t
    }

  }

  init {
    mode = Mode.OUT
    this.bodySize = bodySize
    originalBodySize = bodySize
    eyeSize = bodySize * EYE_SCALE
    paint = Paint()
    paint.isAntiAlias = true
    paint.isFilterBitmap = true
    bubble = Bubble()
    eyes = Eyes(this, creatureInteraction)
    startTime = System.currentTimeMillis()
  }

  fun draw(canvas: Canvas, screenWidth: Float, screenHeight: Float, doEffects: Boolean) {
    val time = System.currentTimeMillis()
    val t = time - startTime
    canvas.save()
    if (mode == Mode.RETURNING) {
      if (t > comeBackTime) {
        system.setSpringStrength(1f)
        system.moveTo(index, 0f, 0f)
        system.setRepulsionStrength(1f)
        mode = Mode.IN
        creatureInteraction.creatureArrived(this, t)
      }
    } else if (mode == Mode.NOTICING) {
      if (t - scareTime > NOTICING_TIME) {
        scareTime = t
        mode = Mode.SCARED
      }
    } else if (mode == Mode.SCARED) {
      system.setRepulsionStrength(0.3f)
      //system.moveTo(index, anticipateX, anticipateY);
      mode = Mode.ANTICIPATING
    } else if (mode == Mode.ANTICIPATING) {
      if (t - scareTime > SCARED_TIME) {
        mode = Mode.LEAVING
      }
    } else if (mode == Mode.LEAVING) {
      val escapeX = (PhysicsSystem.WIDTH.toDouble() * 2.0 * Math.cos(escapeAngle.toDouble())).toFloat()
      val escapeY = (PhysicsSystem.WIDTH.toDouble() * 2.0 * Math.sin(escapeAngle.toDouble())).toFloat()
      system.setRepulsionStrength(1f)
      system.setSpringStrength(2f)
      system.moveTo(index, escapeX, escapeY)
      mode = Mode.OUT
    } else if (mode == Mode.IN && behavior == null) {
      if (doEffects && Math.random() < GROW_CHANCE) {
        behavior = Behavior(MathUtils.random(2f, 3f), t)
        creatureInteraction.notice(this, t)
      }
    }
    if (behavior != null) {
      if (behavior!!.type == BehaviorType.GROW) {
        val elapsed = t - behavior!!.startTime
        val scale: Float
        if (elapsed < GROW_TIME) {
          scale = MathUtils.map(elapsed.toFloat(), 0f, GROW_TIME.toFloat(), 1f, behavior!!.size, Util.PATH_CURVE)
        } else if (elapsed < GROW_TIME + POP_TIME) {
          scale = MathUtils.map(elapsed.toFloat(), GROW_TIME.toFloat(), (GROW_TIME + POP_TIME).toFloat(), behavior!!.size, 1f, POP_INTERPOLATOR)
        } else {
          scale = 1f
          behavior = null
        }
        resize(scale)
      }
    }
    // Offset based on springs
    system.getOffset(index, position)
    canvas.translate(screenWidth / 2, screenHeight / 2)
    canvas.translate(position.x, position.y)
    doDraw(canvas, t)

    canvas.restore()
  }

  open fun doDraw(canvas: Canvas, t: Long) {
    //// Draw body
    paint.color = bodyColor
    bubble.draw(canvas, paint, bodySize, t)

    //// Draw eyes
    paint.color = eyeColor
    eyes.draw(canvas, paint, bodySize, eyeSize, t)
  }

  private fun resize(scale: Float) {
    bodySize = originalBodySize * scale
    eyeSize = bodySize * EYE_SCALE
    system.scaleSize(index, scale)
  }

  @JvmOverloads
  fun reorient(angle: Float = MathUtils.random(0f, MathUtils.TWO_PI)) {
    escapeAngle = angle
    val escapeX = (PhysicsSystem.WIDTH.toDouble() * 2.0 * Math.cos(escapeAngle.toDouble())).toFloat()
    val escapeY = (PhysicsSystem.WIDTH.toDouble() * 2.0 * Math.sin(escapeAngle.toDouble())).toFloat()
    system.forceTo(index, escapeX, escapeY)
  }

  fun comeBack(delay: Long) {
    if (mode!!.hiding()) {
      comeBackTime = System.currentTimeMillis() - startTime + delay
      mode = Mode.RETURNING
      eyes.comingBack(System.currentTimeMillis() - startTime)
    }
  }

  fun hide() {
    if (!mode!!.hiding()) {
      val lastForce = system.getLastForce(index)
      escapeAngle = if (lastForce.x == 0f && lastForce.y == 0f) {
        MathUtils.random(0f, MathUtils.TWO_PI)
      } else {
        Math.atan2(lastForce.y.toDouble(), lastForce.x.toDouble()).toFloat()
      }
      mode = Mode.NOTICING
      scareTime = System.currentTimeMillis() - startTime
      eyes.getScared(scareTime)
    }
  }

  fun getAngleTo(other: Creature): Float {
    return Math.atan2((other.position.y - position.y).toDouble(), (other.position.x - position.x).toDouble()).toFloat()
  }

  fun lookIfAble(t: Long, other: Creature) {
    eyes.lookIfAble(t, other)
  }

  companion object {

    private val NOTICING_TIME: Long = 100
    private val SCARED_TIME: Long = 400
    private val EYE_SCALE = 0.13f

    private val GROW_CHANCE = 0.0002f
    private val GROW_TIME: Long = 4000
    private val POP_TIME: Long = 400
    private val POP_INTERPOLATOR = EaseOutElasticInterpolator()
  }

}
