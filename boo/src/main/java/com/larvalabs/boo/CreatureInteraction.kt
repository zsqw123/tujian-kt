package com.larvalabs.boo

class CreatureInteraction(private val creatures: List<Creature>) {

  private var newCreature: Creature? = null
  private var arrivalTime: Long = 0

  fun creatureArrived(creature: Creature, time: Long) {
    newCreature = creature
    arrivalTime = time
    notice(creature, time)
  }

  fun notice(creature: Creature, time: Long) {
    for (other in creatures) {
      if (other === creature) {
        break
      } else {
        other.lookIfAble(time, creature)
      }
    }
  }

  fun isNewArrival(time: Long): Boolean {
    if (newCreature != null) {
      if (time - arrivalTime > NEW_ARRIVAL_DURATION) {
        newCreature = null
      }
    }
    return newCreature != null
  }

  fun getLookTarget(creature: Creature): Creature {
    return if (newCreature != null && newCreature !== creature && MathUtils.flip(NEW_ARRIVAL_LOOK_CHANCE)) {
      newCreature!!
    } else {
      var target: Creature
      do {
        target = MathUtils.chooseAtRandom(creatures)
      } while (target === creature)
      target
    }
  }

  companion object {

    private const val NEW_ARRIVAL_DURATION: Long = 1500
    private const val NEW_ARRIVAL_LOOK_CHANCE = 0.9f
  }

}
