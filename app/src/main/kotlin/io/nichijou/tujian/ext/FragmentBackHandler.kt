/*
 * Copyright 2016 ikidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nichijou.tujian.ext

import androidx.fragment.app.*
import androidx.viewpager.widget.ViewPager

interface FragmentBackHandler {
  fun onBackPressed(): Boolean
}

fun FragmentManager.handleBackPress(): Boolean {
  val fragments = this.fragments
  for (i in fragments.indices.reversed()) {
    val child = fragments[i]
    if (child.isFragmentBackHandled()) {
      return true
    }
  }
  if (this.backStackEntryCount > 0) {
    this.popBackStack()
    return true
  }
  return false
}

fun Fragment.handleBackPress(): Boolean {
  return this.childFragmentManager.handleBackPress()
}

fun FragmentActivity.handleBackPress(): Boolean {
  return this.supportFragmentManager.handleBackPress()
}

fun ViewPager?.handleBackPress(): Boolean {
  if (this == null) return false
  val adapter = this.adapter ?: return false
  val currentItem = this.currentItem
  val fragment: Fragment?
  fragment = when (adapter) {
    is FragmentPagerAdapter -> adapter.getItem(currentItem)
    is FragmentStatePagerAdapter -> adapter.getItem(currentItem)
    else -> null
  }
  return fragment.isFragmentBackHandled()
}

fun Fragment?.isFragmentBackHandled(): Boolean {
  return this != null
    && this.isVisible
    && this.userVisibleHint //for ViewPager
    && this is FragmentBackHandler
    && (this as FragmentBackHandler).onBackPressed()
}
