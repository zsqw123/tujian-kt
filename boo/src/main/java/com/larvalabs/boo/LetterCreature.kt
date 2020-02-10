package com.larvalabs.boo

import android.content.*
import android.graphics.*

class LetterCreature(context: Context, private val bodyColor: Int, eyeColor: Int, bodySize: Float, system: PhysicsSystem, index: Int, creatureInteraction: CreatureInteraction, svgResource: Int, private val xOffset: Float) : Creature(bodyColor, eyeColor, bodySize, system, index, creatureInteraction) {

  private val bitmap: Bitmap
  private val source = Rect(0, 0, 196, 196)
  private val dest = RectF()

  init {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    bitmap = BitmapFactory.decodeResource(context.resources, svgResource, options)
  }

  override fun doDraw(canvas: Canvas, t: Long) {
    // Just draw the SVG letter
    canvas.save()
    paint.colorFilter = PorterDuffColorFilter(bodyColor, PorterDuff.Mode.SRC_IN)
    val x = xOffset * bodySize
    dest.set(-bodySize + x, -bodySize, bodySize + x, bodySize)
    canvas.drawBitmap(bitmap, source, dest, paint)
    canvas.restore()
  }

}
