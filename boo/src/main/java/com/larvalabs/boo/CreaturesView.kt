package com.larvalabs.boo

import android.content.*
import android.graphics.*
import android.util.*
import android.view.*
import androidx.appcompat.app.*
import kotlin.random.*

class CreaturesView : View {

  private var creatures: MutableList<Creature>? = null
  private lateinit var system: PhysicsSystem

  private var faceVisible = true

  private var introMode = false
  private var bodyColor = Color.BLACK
  private var eyeColor = Color.WHITE
  private var creaturesNum = 10

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {}

  fun setIntroMode(introMode: Boolean) {
    this.introMode = introMode
  }

  fun setCreatureColor(bodyColor: Int, eyeColor: Int) {
    this.bodyColor = bodyColor
    this.eyeColor = eyeColor
  }

  fun setCreatureNum(num: Int) {
    this.creaturesNum = num
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (creatures == null) {
      creatures = ArrayList()
      if (introMode) {
        val letterSize = width / 13f
        val sizes = mutableListOf(width / 6.5f)
        for (i in 1..6) {
          sizes += letterSize
        }
        val yOffsets = floatArrayOf(-width / 6.5f, letterSize, letterSize, letterSize, letterSize, letterSize, letterSize)
        system = PhysicsSystem(width, *sizes.toFloatArray())
        system.setRepulsionFactor(0.25f)
        system.setSpringOffset(0, 0f, yOffsets[0])
        var offset = -3
        for (i in 1..6) {
          system.setSpringOffset(i, letterSize * offset++, yOffsets[i])
        }
        val creatureInteraction = CreatureInteraction(creatures!!)
        for (i in 0..6) {
          val creature: Creature = when (i) {
              1 -> LetterCreature(context, bodyColor, eyeColor, sizes[i], system, i, creatureInteraction, R.drawable.letter_t, Random.nextFloat())
              2 -> LetterCreature(context, bodyColor, eyeColor, sizes[i], system, i, creatureInteraction, R.drawable.letter_u, Random.nextFloat())
              3 -> LetterCreature(context, bodyColor, eyeColor, sizes[i], system, i, creatureInteraction, R.drawable.letter_j, Random.nextFloat())
              4 -> LetterCreature(context, bodyColor, eyeColor, sizes[i], system, i, creatureInteraction, R.drawable.letter_i, Random.nextFloat())
              5 -> LetterCreature(context, bodyColor, eyeColor, sizes[i], system, i, creatureInteraction, R.drawable.letter_a, Random.nextFloat())
              6 -> LetterCreature(context, bodyColor, eyeColor, sizes[i], system, i, creatureInteraction, R.drawable.letter_n, Random.nextFloat())
              else -> Creature(bodyColor, eyeColor, sizes[i], system, i, creatureInteraction)
          }
          creatures!!.add(creature)
        }
        postDelayed({
          setFaceVisible(true)
          ((context as AppCompatActivity).supportFragmentManager.findFragmentByTag(context.getString(R.string.boo_tag)) as? BooFragment?)?.endIntro()
        }, 5600)
      } else {
        val sizes = FloatArray(creaturesNum)
        for (i in 0 until creaturesNum) {
          sizes[i] = MathUtils.random(width / 20, width / 8).toFloat()
        }
        system = PhysicsSystem(width, *sizes)
        val creatureInteraction = CreatureInteraction(creatures!!)
        for (i in 0 until creaturesNum) {
          //                Creature creature = new LetterCreature(getContext(), sizes[i], system, i, creatureInteraction, R.drawable.letter_b);
          val creature = Creature(bodyColor, eyeColor, sizes[i], system, i, creatureInteraction)
          creature.reorient()
          creatures!!.add(creature)
        }
      }
      setFaceVisible(false)
    }
    system.update()
    for (creature in creatures!!) {
      creature.draw(canvas, width.toFloat(), height.toFloat(), !introMode)
    }
    invalidate()
  }

  fun setFaceVisible(faceVisible: Boolean): Boolean {
    if (faceVisible != this.faceVisible) {
      val delays = LongArray(creatures!!.size)
      if (introMode) {
        delays[0] = 0
        delays[1] = 0
        delays[2] = 2000
        delays[3] = 2400
      } else {
        delays[0] = 0
        for (i in 1 until delays.size) {
          if (MathUtils.flip(0.333f)) {
            delays[i] = delays[i - 1] + MathUtils.random(250, 1000)
          } else {
            delays[i] = delays[i - 1] + MathUtils.random(3000, 8000)
          }
        }
      }
      if (!faceVisible && !introMode) {
        // When returning, re-order the creatures randomly
        creatures?.shuffle()
      }
      for (i in creatures!!.indices) {
        val creature = creatures!![i]
        if (faceVisible) {
          creature.hide()
        } else {
          if (!introMode) {
            creature.reorient()
          }
          creature.comeBack(delays[i])
        }
      }
      this.faceVisible = faceVisible
      return true
    } else {
      return false
    }
  }
}
