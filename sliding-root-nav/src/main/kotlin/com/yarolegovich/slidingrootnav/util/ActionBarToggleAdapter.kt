package com.yarolegovich.slidingrootnav.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout
import com.yarolegovich.slidingrootnav.SlidingDrawerLayout

@SuppressLint("ViewConstructor")
class ActionBarToggleAdapter(context: Context, private val drawer: SlidingDrawerLayout) : DrawerLayout(context) {

  override fun openDrawer(gravity: Int) = drawer.openMenu()

  override fun closeDrawer(gravity: Int) = drawer.closeMenu()

  override fun isDrawerVisible(drawerGravity: Int): Boolean = drawer.isMenuOpened()

  override fun getDrawerLockMode(edgeGravity: Int): Int {
    return when {
      drawer.isMenuLocked() && !drawer.isMenuOpened() -> LOCK_MODE_LOCKED_CLOSED
      drawer.isMenuLocked() && drawer.isMenuOpened() -> LOCK_MODE_LOCKED_OPEN
      else -> LOCK_MODE_UNLOCKED
    }
  }
}
