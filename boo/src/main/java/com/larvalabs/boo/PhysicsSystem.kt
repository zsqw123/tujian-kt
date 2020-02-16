package com.larvalabs.boo

import kotlin.math.*

/**
 * Simulates a physical system with damping.
 */
class PhysicsSystem(width: Int, vararg sizes: Float) {

  private var n: Int = 0
  private lateinit var bodies: Array<Body>
  private var springStrength: Float = 0.toFloat()
  private var repulsionStrength: Float = 0.toFloat()
  private var repulsionFactor = 1f

  private var lastUpdate = -1.0

  private val scale: Float

  private class Body internal constructor(internal var size: Float) {

    internal var pos: Point = Point(0f, 0f)
    internal var vel: Point = Point(0f, 0f)
    internal var force: Point = Point(0f, 0f)
    internal var mass: Float = 0.toFloat()
    internal var originalSize: Float = 0.toFloat()

    internal var springPos: Point = Point(0f, 0f)
    internal var springOffset: Point = Point(0f, 0f)
    internal var lastForce = Point(0f, 0f)

    init {
      originalSize = size
      mass = MASS / 100f
    }

    override fun toString(): String {
      return String.format("P(%2.3f, %2.3f) V(%2.3f, %2.3f)", pos.x, pos.y, vel.x, vel.y)
    }

    internal fun distance(other: Body): Float {
      val d = hypot((pos.x - other.pos.x).toDouble(), (pos.y - other.pos.y).toDouble()).toFloat()
      return max(1f, d - size - other.size)
    }

  }

  init {
    scale = width / WIDTH
    init(sizes)
  }

  fun setSpringOffset(index: Int, x: Float, y: Float) {
    val body = bodies[index]
    body.springOffset.x = x / scale
    body.springOffset.y = y / scale
  }

  fun init(sizes: FloatArray) {
    n = sizes.size
    bodies = Array(n) {
      val body = Body(sizes[it] / scale)
      val angle = MathUtils.random(0f, MathUtils.TWO_PI)
      body.pos.x = (cos(angle.toDouble()) * 2.0 * WIDTH.toDouble()).toFloat()
      body.pos.y = (sin(angle.toDouble()) * 2.0 * WIDTH.toDouble()).toFloat()
      body
    }
    springStrength = SPRING_STRENGTH
    repulsionStrength = REPULSION_STRENGTH
  }

  fun update() {
    val time = System.currentTimeMillis() / 1000.0
    val damping = DAMPING
    var springForceX: Float
    var springForceY: Float
    var dampingForceX: Float
    var dampingForceY: Float
    var repulsiveForceX: Float
    var repulsiveForceY: Float
    var distance: Float
    var repulsion: Float
    var distanceSum: Float
    if (lastUpdate >= 0) {
      val t = STEP
      for (i in bodies.indices) {
        val body = bodies[i]
        for (j in bodies.indices) {
          val other = bodies[j]
          if (body !== other) {
            distance = body.distance(other)
            repulsion = repulsionStrength * repulsionFactor / distance
            distanceSum = max(1f, abs(body.pos.x - other.pos.x) + abs(body.pos.y - other.pos.y))
            repulsiveForceX = (body.pos.x - other.pos.x) * repulsion / distanceSum
            repulsiveForceY = (body.pos.y - other.pos.y) * repulsion / distanceSum
            //                        Util.log("Repulsion = " + repulsion + " -> " + repulsiveForceX + ", " + repulsiveForceY);
            body.force.x += repulsiveForceX
            body.force.y += repulsiveForceY
          }
        }
        springForceX = -springStrength * body.mass * (body.pos.x - (body.springPos.x + body.springOffset.x))
        springForceY = -springStrength * body.mass * (body.pos.y - (body.springPos.y + body.springOffset.y))
        dampingForceX = -damping * body.vel.x
        dampingForceY = -damping * body.vel.y
        body.vel.x += t * (springForceX + dampingForceX + body.force.x) / body.mass
        body.vel.y += t * (springForceY + dampingForceY + body.force.y) / body.mass
        body.pos.x += t * body.vel.x
        body.pos.y += t * body.vel.y
        body.lastForce.x = body.force.x
        body.lastForce.y = body.force.y
        body.force.x = 0f
        body.force.y = 0f
      }
    }
    lastUpdate = time
  }

  fun setSpringStrength(factor: Float) {
    springStrength = SPRING_STRENGTH * factor
  }

  fun setRepulsionStrength(factor: Float) {
    repulsionStrength = REPULSION_STRENGTH * factor
  }

  fun setRepulsionFactor(repulsionFactor: Float) {
    this.repulsionFactor = repulsionFactor
  }

  fun moveTo(index: Int, x: Float, y: Float) {
    val body = bodies[index]
    body.springPos.x = x
    body.springPos.y = y
  }

  fun forceTo(index: Int, x: Float, y: Float) {
    val body = bodies[index]
    body.springPos.x = x
    body.springPos.y = y
    body.pos.x = x
    body.pos.y = y
  }

  fun scaleSize(index: Int, factor: Float) {
    val body = bodies[index]
    body.size = body.originalSize * factor
  }

  fun getOffset(index: Int, pos: Point) {
    pos.x = scale * bodies[index].pos.x
    pos.y = scale * bodies[index].pos.y
  }

  fun getLastForce(index: Int): Point {
    return bodies[index].lastForce
  }

  companion object {

    const val WIDTH = 360f

    // Larger value means that the bodies are heavier, harder to move/stop
    private const val MASS = 1f

    private const val SPRING_STRENGTH = 250f
    private const val DAMPING = 0.75f

    private const val REPULSION_STRENGTH = 5000f

    private const val STEP = 1 / 60f
  }

}
