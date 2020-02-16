package com.larvalabs.boo

class Point {

  var x: Float = 0.toFloat()
  var y: Float = 0.toFloat()

  constructor() {
    x = 0f
    y = 0f
  }

  constructor(other: Point) {
    x = other.x
    y = other.y
  }

  constructor(x: Float, y: Float) {
    this.x = x
    this.y = y
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false

    val point = o as Point?

    if (point!!.x.compareTo(x) != 0) return false
    return point.y.compareTo(y) == 0

  }

  override fun hashCode(): Int {
    var result = if (x != +0.0f) java.lang.Float.floatToIntBits(x) else 0
    result = 31 * result + if (y != +0.0f) java.lang.Float.floatToIntBits(y) else 0
    return result
  }

  override fun toString(): String {
    return "($x, $y)"
  }

  companion object {

    val ZERO = Point(0f, 0f)
  }
}
