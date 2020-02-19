package io.nichijou.tujian.ext

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.nichijou.tujian.R
import java.util.*


fun AppCompatActivity.replaceFragmentInActivity(
  fragment: Fragment,
  @IdRes wrapperIdRes: Int = R.id.container
) {
  supportFragmentManager.beginTransaction()
    .replace(wrapperIdRes, fragment)
    .commitAllowingStateLoss()
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
  if (!childFragmentManager.isDestroyed){
    val transaction = childFragmentManager.beginTransaction()
    val name = UUID.randomUUID().toString()
    transaction.add(wrapperIdRes, fragment, name)
      .addToBackStack(name)
      .commitAllowingStateLoss()
  }
 }
