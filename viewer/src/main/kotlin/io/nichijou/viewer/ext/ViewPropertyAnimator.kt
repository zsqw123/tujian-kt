package io.nichijou.viewer.ext

import android.animation.*
import android.view.*

fun ViewPropertyAnimator.setAnimatorListener(
  onAnimationEnd: ((Animator?) -> Unit)? = null,
  onAnimationStart: ((Animator?) -> Unit)? = null
) = this.setListener(
  object : AnimatorListenerAdapter() {
    override fun onAnimationEnd(animation: Animator?) {
      onAnimationEnd?.invoke(animation)
    }

    override fun onAnimationStart(animation: Animator?) {
      onAnimationStart?.invoke(animation)
    }
  })
