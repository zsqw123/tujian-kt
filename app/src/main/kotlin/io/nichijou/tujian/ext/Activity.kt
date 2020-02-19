package io.nichijou.tujian.ext

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.billy.android.swipe.SmartSwipeWrapper
import com.billy.android.swipe.SwipeConsumer
import com.billy.android.swipe.listener.SwipeListener
import io.nichijou.tujian.R
import io.nichijou.tujian.ui.MainActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


fun AppCompatActivity.replaceFragmentInActivity(
  fragment: Fragment,
  @IdRes wrapperIdRes: Int = R.id.container
) {
  if (MainActivity.swipeConsumer != null && MainActivity.swipeConsumer!!.isOpened) {
    MainActivity.swipeConsumer!!.smoothClose()
    MainActivity.swipeConsumer!!.addListener(object : SwipeListener {
      override fun onSwipeStart(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, direction: Int) {}
      override fun onSwipeProcess(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, direction: Int, settling: Boolean, progress: Float) {}
      override fun onConsumerAttachedToWrapper(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?) {}
      override fun onConsumerDetachedFromWrapper(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?) {}
      override fun onSwipeStateChanged(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, state: Int, direction: Int, progress: Float) {}
      override fun onSwipeRelease(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, direction: Int, progress: Float, xVelocity: Float, yVelocity: Float) {}
      override fun onSwipeOpened(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, direction: Int) {}
      override fun onSwipeClosed(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, direction: Int) {
        supportFragmentManager.beginTransaction().setTransition(TRANSIT_FRAGMENT_FADE)
          .replace(wrapperIdRes, fragment)
          .commitAllowingStateLoss()
        MainActivity.swipeConsumer!!.removeAllListeners()
      }
    })
  } else {
    supportFragmentManager.beginTransaction()
      .replace(wrapperIdRes, fragment)
      .commitAllowingStateLoss()
  }
}

fun AppCompatActivity.addFragmentToActivity(
  fragment: Fragment,
  @IdRes wrapperIdRes: Int = R.id.container,
  tag: String? = null,
  hideBefore: Boolean = false
) {
  val transaction = supportFragmentManager.beginTransaction()
  if (hideBefore) {
    supportFragmentManager.fragments.forEach {
      if (!it.isHidden) {
        transaction.hide(it)
      }
    }
  }
  val name = UUID.randomUUID().toString()
  transaction.add(wrapperIdRes, fragment, tag ?: name)
    .addToBackStack(tag ?: name)
    .commitAllowingStateLoss()
}


fun Fragment.target(): AppCompatActivity {
  return activity as? AppCompatActivity? ?: throw IllegalStateException("fragment host is null.")
}

fun Fragment.replaceFragmentInActivity(
  fragment: Fragment,
  @IdRes wrapperIdRes: Int = R.id.container
) {
  target().replaceFragmentInActivity(fragment, wrapperIdRes)
}

fun Fragment.addFragmentToActivity(
  fragment: Fragment,
  @IdRes wrapperIdRes: Int = R.id.container,
  tag: String? = null,
  hideBefore: Boolean = false
) {
  target().addFragmentToActivity(fragment, wrapperIdRes, tag, hideBefore)
}

fun Fragment.replaceFragment(
  fragment: Fragment,
  @IdRes wrapperIdRes: Int
) {
  childFragmentManager.beginTransaction()
    .replace(wrapperIdRes, fragment)
    .commitAllowingStateLoss()
}

fun Fragment.addFragment(
  fragment: Fragment,
  @IdRes wrapperIdRes: Int
) {
  if (!childFragmentManager.isDestroyed) {
    val transaction = childFragmentManager.beginTransaction()
    val name = UUID.randomUUID().toString()
    transaction.add(wrapperIdRes, fragment, name)
      .addToBackStack(name)
      .commitAllowingStateLoss()
  }
}
