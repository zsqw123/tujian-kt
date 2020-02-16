package com.yarolegovich.slidingrootnav.util

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout

class DrawerToggleListenerAdapter(private val drawerListener: DrawerLayout.DrawerListener, private val drawer: View) : DrawerLayout.DrawerListener {
  override fun onDrawerStateChanged(newState: Int) = drawerListener.onDrawerStateChanged(newState)
  override fun onDrawerSlide(drawerView: View, slideOffset: Float) = drawerListener.onDrawerSlide(drawer, slideOffset)
  override fun onDrawerClosed(drawerView: View) = drawerListener.onDrawerClosed(drawer)
  override fun onDrawerOpened(drawerView: View) = drawerListener.onDrawerOpened(drawer)
}
