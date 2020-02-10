package com.yarolegovich.slidingrootnav.menu

import android.view.*


abstract class DrawerItem<T : DrawerAdapter.ViewHolder>(open val id: Int = -1, open var isSelectable: Boolean = true) {

  open var isChecked = false

  abstract fun createViewHolder(parent: ViewGroup): T

  abstract fun bindViewHolder(holder: T)

  open fun setChecked(isChecked: Boolean): DrawerItem<*> {
    this.isChecked = isChecked
    return this
  }

}
